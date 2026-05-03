package io.tomaszstr.simplestockmarket.audit;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public record LogEntryRecord(
        @JsonProperty("type") String type,
        @JsonProperty("wallet_id") String walletId,
        @JsonProperty("stock_name") String stockName
) {
    public LogEntryRecord {
        Objects.requireNonNull(type, "Action type must not be null");
        Objects.requireNonNull(walletId, "Wallet ID must not be null");
        Objects.requireNonNull(stockName, "Stock name must not be null");
    }
}