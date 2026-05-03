package io.tomaszstr.simplestockmarket.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = GlobalExceptionHandlerIntegrationTest.ExceptionTriggerController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvcTester mockMvc;

    @TestConfiguration
    static class Config {
        @Bean
        public ExceptionTriggerController exceptionTriggerController() {
            return new ExceptionTriggerController();
        }
    }

    @RestController
    static class ExceptionTriggerController {
        @GetMapping("/trigger-illegal")
        void trigger() {
            throw new IllegalArgumentException("Bad arg");
        }

        @GetMapping("/trigger-market")
        void triggerMarket() {
            throw new SimpleStockMarketException("Market down");
        }

        @GetMapping("/trigger-generic")
        void triggerGeneric() {
            throw new RuntimeException("Unexpected");
        }
    }

    @Test
    void shouldHandleSimpleStockMarketExceptionAs500() {
        // when & then
        mockMvc.get().uri("/trigger-market")
                .assertThat().hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .body().asString().isEqualTo("An internal market error occurred");
    }

    @Test
    void shouldHandleGenericExceptionAs500() {
        // when & then
        mockMvc.get().uri("/trigger-generic")
                .assertThat().hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .body().asString().isEqualTo("An unexpected error occurred");
    }
}
