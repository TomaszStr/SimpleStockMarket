package io.tomaszstr.simplestockmarket.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class GlobalExceptionHandlerUnitTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleIllegalArgument should return 400 with exception message")
    void givenIllegalArgument_whenHandle_thenReturnBadRequest() {
        // given
        var exception = new IllegalArgumentException("Invalid state");

        // when
        var response = handler.handleIllegalArgument(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Invalid state");
    }

    @Test
    @DisplayName("handleValidationExceptions should aggregate multiple errors into a map")
    void givenValidationErrors_whenHandle_thenAggregateToMap() {
        // given
        var bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "stocks[0].name", "must not be blank"));
        bindingResult.addError(new FieldError("request", "stocks[0].quantity", "must be positive"));

        var exception = new MethodArgumentNotValidException(null, bindingResult);

        // when
        var response = handler.handleValidationExceptions(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("stocks[0].name", "must not be blank")
                .containsEntry("stocks[0].quantity", "must be positive");
    }

    @Test
    @DisplayName("handleNoResourceFound should return 404 when resource path does not exist")
    void givenNoResourceFoundException_whenHandle_thenReturnNotFound() {
        // given
        var path = "/api/v1/non-existent";
        var exception = new NoResourceFoundException(HttpMethod.GET, path, path);

        // when
        var response = handler.handleNoResourceFound(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }
}