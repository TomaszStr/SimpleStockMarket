package io.tomaszstr.simplestockmarket.chaos;

import io.tomaszstr.simplestockmarket.config.SystemManager;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChaosController {
    private final SystemManager systemManager;

    @PostMapping("/chaos")
    public ResponseEntity<String> unleashChaos() {
        log.error("CRITICAL: Chaos endpoint triggered. Instance is going down!");

        // Spin up a parallel thread to kill the app, allowing the HTTP 200 response to fire first
        Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(Duration.ofMillis(100));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            log.warn("Chaos timer expired. Executing System.exit(1)...");
            systemManager.terminate(1);
        });

        return ResponseEntity.ok("Chaos initiated. Instance terminating.");
    }
}
