package io.tomaszstr.simplestockmarket.bank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StockInfoTest {

    private static final String VALID_NAME = "AAPL";
    private static final String EMPTY_NAME = "";
    private static final String BLANK_NAME = "  ";
    private static final long VALID_QUANTITY = 100L;
    private static final long NEGATIVE_QUANTITY = -1L;
    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create StockInfo when data is valid")
    void givenValidData_whenCreatingStockInfo_thenSuccess() {
        // when
        var stock = new StockInfo(VALID_NAME, VALID_QUANTITY);

        // then
        assertThat(stock.name()).isEqualTo(VALID_NAME);
        assertThat(stock.quantity()).isEqualTo(VALID_QUANTITY);
    }

    @ParameterizedTest
    @MethodSource("provideNullStockData")
    @DisplayName("Constructor should throw NPE for null mandatory fields")
    void givenNullData_whenCreatingStockInfo_thenThrowNullPointerException(
            String name, long quantity, String expectedMessage) {

        // when & then
        assertThatThrownBy(() -> new StockInfo(name, quantity))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining(expectedMessage);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidValidationData")
    @DisplayName("Jakarta validation should detect blank names or negative quantities")
    void givenInvalidData_whenValidatingStockInfo_thenDetectViolations(
            String name, long quantity, String expectedMessage) {

        // given
        StockInfo stockInfo = new StockInfo(name, quantity);

        // when
        var violations = validator.validate(stockInfo);

        // then
        assertThat(violations)
                .as("Validation violations should be present for: %s, %d", name, quantity)
                .isNotEmpty();

        boolean messageFound = violations.stream()
                .anyMatch(v -> v.getMessage().contains(expectedMessage));

        assertThat(messageFound)
                .as("Expected validation message [%s] was not found in violations", expectedMessage)
                .isTrue();
    }

    private static Stream<Arguments> provideNullStockData() {
        return Stream.of(
                Arguments.of(null, VALID_QUANTITY, "Stock name must not be null")
        );
    }

    private static Stream<Arguments> provideInvalidValidationData() {
        return Stream.of(
                Arguments.of(EMPTY_NAME, VALID_QUANTITY, "Stock name must not be blank"),
                Arguments.of(BLANK_NAME, VALID_QUANTITY, "Stock name must not be blank"),
                Arguments.of(VALID_NAME, NEGATIVE_QUANTITY,
                        "Bank stock quantity must not be negative")
        );
    }
}