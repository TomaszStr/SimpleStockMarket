package io.tomaszstr.simplestockmarket.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LogEntryRecordTest {
    private static final String TYPE = "buy";
    private static final String WALLET_ID = "wallet-123";
    private static final String STOCK = "AAPL";

    @ParameterizedTest
    @MethodSource("provideNullFields")
    @DisplayName("Should throw NullPointerException with specific message when field is null")
    void givenNullField_whenCreatingRecord_thenThrowException(String type, String walletId,
                                                              String stockName,
                                                              String expectedMessage) {

        // when & then
        assertThatThrownBy(() -> new LogEntryRecord(type, walletId, stockName)).isInstanceOf(
                NullPointerException.class).hasMessage(expectedMessage);
    }

    private static Stream<Arguments> provideNullFields() {
        return Stream.of(Arguments.of(null, WALLET_ID, STOCK, "Action type must not be null"),
                Arguments.of(TYPE, null, STOCK, "Wallet ID must not be null"),
                Arguments.of(TYPE, WALLET_ID, null, "Stock name must not be null"));
    }

    @Test
    @DisplayName("Should create record successfully when all fields are provided")
    void givenValidData_whenCreatingRecord_thenFieldsAreSet() {
        // when
        var logEntryRecord = new LogEntryRecord(TYPE, WALLET_ID, STOCK);

        // then
        assertThat(logEntryRecord.type()).isEqualTo(TYPE);
        assertThat(logEntryRecord.walletId()).isEqualTo(WALLET_ID);
        assertThat(logEntryRecord.stockName()).isEqualTo(STOCK);
    }
}