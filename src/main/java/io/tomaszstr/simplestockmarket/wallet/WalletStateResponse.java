package io.tomaszstr.simplestockmarket.wallet;

import java.util.List;
import java.util.UUID;

public record WalletStateResponse(UUID id, List<StockHoldings> stocks) {

    public static WalletStateResponse of(Wallet wallet) {
        return new WalletStateResponse(wallet.getId(),
                wallet.getHoldings().stream().map(StockHoldings::of).toList());
    }
}
