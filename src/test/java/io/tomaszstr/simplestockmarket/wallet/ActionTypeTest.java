package io.tomaszstr.simplestockmarket.wallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ActionTypeTest {
    @ParameterizedTest
    @CsvSource({
            "buy, BUY",
            "BUY, BUY",
            "sell, SELL",
            "SELL, SELL"
    })
    @DisplayName("Should parse valid strings regardless of case")
    void givenValidString_whenFromString_thenReturnCorrectEnum(String input, ActionType expected) {
        // when
        ActionType result = ActionType.fromString(input);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should throw exception when input is null")
    void givenNull_whenFromString_thenThrowException() {
        // when & then
        assertThatThrownBy(() -> ActionType.fromString(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be null");
    }

    @ParameterizedTest
    @ValueSource(strings = {"trade", "hold", "", " "})
    @DisplayName("Should throw exception for invalid action strings")
    void givenInvalidString_whenFromString_thenThrowException(String invalid) {
        // when & then
        assertThatThrownBy(() -> ActionType.fromString(invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid action type");
    }
}