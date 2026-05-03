package io.tomaszstr.simplestockmarket.wallet;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/{wallet_id}/stocks/{stock_name}")
    public ResponseEntity<Void> operation(
            @PathVariable("wallet_id") UUID walletId,
            @PathVariable("stock_name") String ticker,
            @RequestBody WalletOperationRequest request) {

        ActionType actionType = ActionType.fromString(request.type());
        walletService.performAction(walletId, ticker, actionType);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{wallet_id}")
    public ResponseEntity<WalletStateResponse> getWallet(@PathVariable("wallet_id") UUID walletId) {
        return ResponseEntity.ok(walletService.getWalletState(walletId));
    }

    @GetMapping("/{wallet_id}/stocks/{stock_name}")
    public ResponseEntity<Long> getStockQuantity(
            @PathVariable("wallet_id") UUID walletId,
            @PathVariable("stock_name") String ticker) {

        return ResponseEntity.ok(walletService.getStockQuantity(walletId, ticker));
    }
}
