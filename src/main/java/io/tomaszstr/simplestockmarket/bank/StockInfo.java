package io.tomaszstr.simplestockmarket.bank;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

public record StockInfo(
        @NotBlank(message = "Stock name must not be blank")
        @JsonProperty("name")
        String name,
        @Min(value = 0, message = "Bank stock quantity must not be negative")
        @JsonProperty("quantity")
        long quantity
) {
    public StockInfo {
        Objects.requireNonNull(name, "Stock name must not be null");
    }
}
