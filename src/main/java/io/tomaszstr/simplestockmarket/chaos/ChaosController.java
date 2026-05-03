package io.tomaszstr.simplestockmarket.chaos;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ChaosController {

    @PostMapping("/chaos")
    public ResponseEntity<String> unleashChaos() {
        log.error("CRITICAL: Chaos endpoint triggered. Instance is going down!");

        // Spin up a parallel thread to kill the app, allowing the HTTP 200 response to fire first
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.exit(1); // Brute force JVM termination
        }).start();

        return ResponseEntity.ok("Chaos initiated. Instance terminating.");
    }
}
