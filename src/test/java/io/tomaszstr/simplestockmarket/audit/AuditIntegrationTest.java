package io.tomaszstr.simplestockmarket.audit;

import static org.assertj.core.api.Assertions.assertThat;

import io.tomaszstr.simplestockmarket.integration.BaseIntegrationTest;
import io.tomaszstr.simplestockmarket.wallet.ActionType;
import java.util.UUID;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AuditIntegrationTest extends BaseIntegrationTest {

    private static final String ENDPOINT_LOG = "/log";
    private static final UUID WALLET_ID = UUID.randomUUID();
    private static final String TICKER = "CBDADSA";
    private static final String ACTION_BUY = "buy";
    private static final Long QUANTITY = 50L;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private AuditService auditService;

    @BeforeEach
    void clean() {
        auditRepository.deleteAll();
    }

    @Test
    @DisplayName("Should return empty log list when no operations have occurred")
    void givenEmptyDatabase_whenGetLog_thenReturnEmptyLogWrapper() {
        // when & then
        mockMvc.get().uri(ENDPOINT_LOG)
                .assertThat().hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.log",
                        log -> assertThat(log).asInstanceOf(InstanceOfAssertFactories.LIST)
                                .isEmpty());
    }

    @Test
    @DisplayName("Should return recorded logs with correct field names when operations succeed")
    void givenSuccessfulOperationRecorded_whenGetLog_thenReturnFormattedJson() {
        // given
        auditService.recordSuccessfulOperation(WALLET_ID, TICKER, ActionType.BUY, QUANTITY);

        // when & then
        mockMvc.get().uri(ENDPOINT_LOG)
                .assertThat().hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.log[0].type",
                        type -> assertThat(type).asString().isEqualTo(ACTION_BUY))
                .hasPathSatisfying("$.log[0].wallet_id",
                        id -> assertThat(id).asString().isEqualTo(WALLET_ID.toString()))
                .hasPathSatisfying("$.log[0].stock_name",
                        name -> assertThat(name).asString().isEqualTo(TICKER));
    }

    @Test
    @DisplayName("Should maintain chronological order of occurrence in the audit log")
    void givenMultipleOperations_whenGetLog_thenReturnInOrderOfOccurrence() {
        // given
        UUID secondWallet = UUID.randomUUID();
        auditService.recordSuccessfulOperation(WALLET_ID, "STOCK1", ActionType.BUY, 10L);
        auditService.recordSuccessfulOperation(secondWallet, "STOCK2", ActionType.SELL, 5L);

        // when & then
        mockMvc.get().uri(ENDPOINT_LOG)
                .assertThat().hasStatusOk().bodyJson()
                .hasPathSatisfying("$.log",
                        logs -> assertThat(logs).asInstanceOf(InstanceOfAssertFactories.LIST)
                                .hasSize(2))
                .hasPathSatisfying("$.log[0].stock_name",
                        name -> assertThat(name).asString().isEqualTo("STOCK1"))
                .hasPathSatisfying("$.log[1].stock_name",
                        name -> assertThat(name).asString().isEqualTo("STOCK2"));
    }
}
