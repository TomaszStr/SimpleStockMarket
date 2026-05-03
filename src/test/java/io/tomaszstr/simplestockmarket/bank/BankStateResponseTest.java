package io.tomaszstr.simplestockmarket.bank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BankStateResponseTest {

    private static final String TICKER = "AAPL";
    private static final long QUANTITY = 100L;
    private static final StockInfo SAMPLE_STOCK = new StockInfo(TICKER, QUANTITY);

    @Test
    @DisplayName("Should return empty list when null stocks provided")
    void givenNullStocks_whenCreatingResponse_thenStocksIsNotNullAndEmpty() {
        // given
        List<StockInfo> nullStocks = null;

        // when
        var response = new BankStateResponse(nullStocks);

        // then
        assertThat(response.stocks())
                .isNotNull()
                .isEmpty();
    }

    @Test
    @DisplayName("Should create immutable copy when valid list provided")
    void givenValidStocks_whenCreatingResponse_thenStocksAreStored() {
        // given
        var stocks = List.of(SAMPLE_STOCK);

        // when
        var response = new BankStateResponse(stocks);

        // then
        assertThat(response.stocks()).hasSize(1);
        assertThat(response.stocks().getFirst()).isEqualTo(SAMPLE_STOCK);
    }

    @Test
    @DisplayName("Should be resistant to modification of the original list")
    void givenMutableList_whenListModifiedAfterCreation_thenResponseStocksRemainUnchanged() {
        // given
        var mutableList = new ArrayList<StockInfo>();
        mutableList.add(SAMPLE_STOCK);
        var response = new BankStateResponse(mutableList);

        // when
        mutableList.clear();

        // then
        assertThat(response.stocks())
                .as("The record should hold a defensive copy, not a reference to the mutable list")
                .hasSize(1)
                .containsExactly(SAMPLE_STOCK);
    }

    @Test
    @DisplayName("Should throw exception when attempting to modify the returned list")
    void givenValidResponse_whenModifyingReturnedList_thenThrowUnsupportedOperationException() {
        // given
        var response = new BankStateResponse(List.of(SAMPLE_STOCK));
        List<StockInfo> resultStocks = response.stocks();
        StockInfo stockInfo = new StockInfo("GOOG", 10L);

        // when & then
        assertThatThrownBy(() -> resultStocks.add(stockInfo))
                .as("List.copyOf should return an unmodifiable list")
                .isInstanceOf(UnsupportedOperationException.class);
    }
}