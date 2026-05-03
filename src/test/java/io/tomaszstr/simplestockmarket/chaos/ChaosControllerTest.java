package io.tomaszstr.simplestockmarket.chaos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import io.tomaszstr.simplestockmarket.config.SystemManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ChaosControllerTest {

    @Mock
    private SystemManager systemManager;

    @InjectMocks
    private ChaosController chaosController;

    @Test
    @DisplayName("Should return 200 OK and trigger termination when chaos is unleashed")
    void givenAppIsRunning_whenUnleashChaos_thenResponseIsOkAndExitTriggered() {
        // when
        var responseEntity = chaosController.unleashChaos();

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).contains("Chaos initiated");

        // Verify the manager was called.
        verify(systemManager, timeout(500)).terminate(1);
    }
}