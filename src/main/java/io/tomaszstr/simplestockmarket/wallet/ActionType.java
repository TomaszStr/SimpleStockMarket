package io.tomaszstr.simplestockmarket.wallet;

public enum ActionType {
    BUY,
    SELL;

    public static ActionType fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Action type must not be null");
        }
        return switch (value.toLowerCase()) {
            case "buy" -> BUY;
            case "sell" -> SELL;
            default -> throw new IllegalArgumentException("Invalid action type: " + value);
        };
    }
}