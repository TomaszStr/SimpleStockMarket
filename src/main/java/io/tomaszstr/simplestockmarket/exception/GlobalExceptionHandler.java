package io.tomaszstr.simplestockmarket.exception;

import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles Jakarta Bean Validation failures (triggered by @Valid).
     * Aggregates all field-level errors into a map for precise client feedback.
     *
     * @param ex The validation exception containing binding results.
     * @return A {@link ResponseEntity} with status 400 (Bad Request) and a map of field errors.
     *      Example: { "stocks[0].name": "must not be blank" }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null
                                ? fieldError.getDefaultMessage()
                                : "Invalid value",
                        (existing, replacement) -> existing + ", " + replacement
                ));

        log.warn("Validation failed for request: {}", errors);
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handles JSON parsing errors (e.g., sending a string where a number is expected).
     *
     * @param ex The parsing exception.
     * @return A 400 Bad Request with a sanitized message.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleInvalidJson(HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON request received: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("Invalid request format or data types");
    }

    /**
     * Handles standard Java illegal argument exceptions.
     * Typically thrown during business logic checks or domain object instantiation.
     *
     * @param ex The illegal argument exception.
     * @return A {@link ResponseEntity} with status 400 (Bad Request) and the error message.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    /**
     * Handles cases where a requested stock ticker is not found in the system.
     * This ensures the API returns the mandatory 404 status when an operation
     * targets a non-existent stock.
     *
     * @param ex The stock not found exception.
     * @return A {@link ResponseEntity} with status 404 (Not Found) and the error message.
     */
    @ExceptionHandler(StockNotFoundException.class)
    public ResponseEntity<String> handleNotFound(StockNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Handles domain-specific exceptions unique to the Stock Market application.
     * This ensures internal system failures are logged correctly while hiding sensitive
     * implementation details from the client.
     *
     * @param ex The custom system exception.
     * @return A {@link ResponseEntity} with status 500 (Internal Server Error)
     *      and a sanitized message.
     */
    @ExceptionHandler(SimpleStockMarketException.class)
    public ResponseEntity<String> handleStockMarketException(SimpleStockMarketException ex) {
        log.error("Market operation failed: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body("An internal market error occurred");
    }

    /**
     * The final "catch-all" handler for any unexpected exceptions.
     * Prevents the application from leaking stack traces to the API consumer.
     *
     * @param ex The unhandled exception.
     * @return A {@link ResponseEntity} with status 500 (Internal Server Error)
     *      and a generic message.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        log.error("Unhandled exception caught", ex);
        return ResponseEntity.internalServerError().body("An unexpected error occurred");
    }
}
