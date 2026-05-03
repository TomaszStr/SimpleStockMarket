package io.tomaszstr.simplestockmarket.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import io.tomaszstr.simplestockmarket.exception.SimpleStockMarketException;
import io.tomaszstr.simplestockmarket.wallet.ActionType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    private static final UUID WALLET_ID = UUID.randomUUID();
    private static final String STOCK_NAME = "CBDADSA";
    private static final Long QUANTITY = 100L;
    private static final String BUY_TYPE = "buy";
    private static final ActionType ACTION = ActionType.BUY;

    @Mock
    private AuditRepository auditRepository;

    @InjectMocks
    private AuditService auditService;

    @ParameterizedTest
    @EnumSource(ActionType.class)
    @DisplayName("Should save audit entry when valid transaction occurs")
    void givenValidTransaction_whenRecordSuccessfulOperation_thenInvokeRepositorySave(
            ActionType actionType) {
        // when
        auditService.recordSuccessfulOperation(WALLET_ID, STOCK_NAME, actionType, QUANTITY);

        // then
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditRepository).save(captor.capture());

        AuditLog savedEntry = captor.getValue();
        assertThat(savedEntry.getWalletId()).isEqualTo(WALLET_ID);
        assertThat(savedEntry.getTicker()).isEqualTo(STOCK_NAME);
        assertThat(savedEntry.getActionType()).isEqualTo(actionType);
    }

    @Test
    @DisplayName("Should throw custom exception when database fails during recording")
    void givenDatabaseError_whenRecordSuccessfulOperation_thenThrowSimpleStockMarketException() {
        // given
        doThrow(new RuntimeException("DB Failure")).when(auditRepository).save(any());

        // when & then
        assertThatThrownBy(() -> auditService.recordSuccessfulOperation(WALLET_ID, STOCK_NAME,
                ACTION, QUANTITY)).isInstanceOf(SimpleStockMarketException.class)
                .hasMessageContaining("Failed to write audit log entry");
    }

    @Test
    @DisplayName("Should return formatted log response in order of occurrence")
    void givenExistingLogsInDb_whenGetFullAuditLog_thenReturnMappedResponseInOrder() {
        // given
        AuditLog entry = createAuditEntry();
        given(auditRepository.findAllByOrderByIdAsc()).willReturn(List.of(entry));

        // when
        var response = auditService.getFullAuditLog();

        // then
        assertThat(response.log()).hasSize(1);
        assertThat(response.log().getFirst().type()).isEqualTo(BUY_TYPE);
        assertThat(response.log().getFirst().walletId()).isEqualTo(WALLET_ID.toString());
        verify(auditRepository).findAllByOrderByIdAsc();
    }

    @Test
    @DisplayName("Should throw SimpleStockMarketException when database fails")
    void getFullAuditLog_whenDatabaseFails_shouldThrowCustomException() {
        // given
        String errorMessage = "Database connection timed out";
        given(auditRepository.findAllByOrderByIdAsc()).willThrow(
                new RuntimeException(errorMessage));

        // when & then
        assertThatThrownBy(() -> auditService.getFullAuditLog())
                .isInstanceOf(SimpleStockMarketException.class)
                .hasMessage("Could not retrieve audit logs")
                .hasCauseInstanceOf(RuntimeException.class);

        // Verify the repository was actually called
        verify(auditRepository).findAllByOrderByIdAsc();
    }

    @Test
    @DisplayName("Should return empty list when no logs exist")
    void getFullAuditLog_whenNoLogs_shouldReturnEmptyResponse() {
        // given
        given(auditRepository.findAllByOrderByIdAsc()).willReturn(List.of());

        // when
        AuditLogResponse response = auditService.getFullAuditLog();

        // then
        assertThat(response.log()).isEmpty();
        verify(auditRepository).findAllByOrderByIdAsc();
    }

    private AuditLog createAuditEntry() {
        return AuditLog.builder()
                .walletId(WALLET_ID)
                .ticker(STOCK_NAME)
                .actionType(ACTION)
                .quantity(QUANTITY)
                .build();
    }
}