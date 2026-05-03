package io.tomaszstr.simplestockmarket.exception;

public class SimpleStockMarketException extends RuntimeException {
    public SimpleStockMarketException(String message, Exception e) {
        super(message, e);
    }
}
