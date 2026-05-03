package io.tomaszstr.simplestockmarket.bank;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

public record BankStateRequest(
        @NotNull(message = "Stocks list must not be null")
        @Valid
        List<StockInfo> stocks
) {
    public BankStateRequest {
        Objects.requireNonNull(stocks, "Stocks list must not be null");
    }
}
