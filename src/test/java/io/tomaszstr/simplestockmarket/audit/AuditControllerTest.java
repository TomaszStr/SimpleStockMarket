package io.tomaszstr.simplestockmarket.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class AuditControllerTest {

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuditController auditController;

    @Test
    @DisplayName("Should return 200 OK with audit logs when requested")
    void givenLogsExist_whenGetAuditLog_thenReturnOkWithData() {
        // given
        var logEntry = new LogEntryRecord("buy", "uuid-123", "AAPL");
        var expectedResponse = new AuditLogResponse(List.of(logEntry));
        given(auditService.getFullAuditLog()).willReturn(expectedResponse);

        // when
        var responseEntity = auditController.getAuditLog();

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().log()).hasSize(1);
        assertThat(responseEntity.getBody().log().getFirst().stockName()).isEqualTo("AAPL");
        verify(auditService).getFullAuditLog();
    }
}