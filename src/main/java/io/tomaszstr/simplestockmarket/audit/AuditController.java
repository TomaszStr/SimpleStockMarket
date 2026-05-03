package io.tomaszstr.simplestockmarket.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/log")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<AuditLogResponse> getAuditLog() {
        log.info("GET /log - Request received to fetch audit history");

        AuditLogResponse response = auditService.getFullAuditLog();

        return ResponseEntity.ok(response);
    }
}
