package io.tomaszstr.simplestockmarket.audit;

import io.tomaszstr.simplestockmarket.exception.SimpleStockMarketException;
import io.tomaszstr.simplestockmarket.wallet.ActionType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;

    /**
     * Public reusable method for other packages (e.g., Wallet package)
     * to record a successful transaction.
     */
    @Transactional
    public void recordSuccessfulOperation(UUID walletId, String ticker, ActionType actionType,
                                          Long quantity) {
        log.debug("Recording audit log for wallet: {}, action: {}, stock: {}", walletId, actionType,
                ticker);

        try {
            AuditLog auditLog = AuditLog.builder()
                    .walletId(walletId)
                    .ticker(ticker)
                    .actionType(actionType)
                    .quantity(quantity)
                    .build();

            auditRepository.save(auditLog);
            log.info("Successfully recorded audit entry for wallet {}", walletId);
        } catch (Exception e) {
            log.error("Failed to record audit log for wallet: {}", walletId, e);
            throw new SimpleStockMarketException("Failed to write audit log entry", e);
        }
    }

    /**
     * Retrieves the full audit log, formatted exactly to the API specification.
     */
    @Transactional(readOnly = true)
    public AuditLogResponse getFullAuditLog() {
        log.debug("Fetching complete audit log");

        try {
            var logs = auditRepository.findAllByOrderByIdAsc().stream()
                    .map(this::mapToRecord)
                    .toList();
            return new AuditLogResponse(logs);
        } catch (Exception e) {
            log.error("Database error while fetching audit logs", e);
            throw new SimpleStockMarketException("Could not retrieve audit logs", e);
        }
    }

    private LogEntryRecord mapToRecord(AuditLog entity) {
        return new LogEntryRecord(entity.getActionType().name().toLowerCase(),
                entity.getWalletId().toString(), entity.getTicker());
    }
}