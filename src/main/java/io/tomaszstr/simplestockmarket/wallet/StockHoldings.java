package io.tomaszstr.simplestockmarket.wallet;

public record StockHoldings(String name, long quantity) {
    public static StockHoldings of(WalletInventory inventory) {
        return new StockHoldings(inventory.getId().ticker(), inventory.getQuantity());
    }
}