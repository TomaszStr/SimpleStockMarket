package io.tomaszstr.simplestockmarket.bank;

import java.util.List;

public record BankStateResponse(List<StockInfo> stocks) {
    public BankStateResponse {
        stocks = stocks == null ? List.of() : List.copyOf(stocks);
    }
}
