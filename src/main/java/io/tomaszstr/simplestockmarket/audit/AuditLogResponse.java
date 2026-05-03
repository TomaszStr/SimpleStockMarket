package io.tomaszstr.simplestockmarket.audit;

import java.util.List;

public record AuditLogResponse(
        List<LogEntryRecord> log
) {
}
