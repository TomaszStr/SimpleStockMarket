package io.tomaszstr.simplestockmarket.wallet;

import io.tomaszstr.simplestockmarket.audit.AuditService;
import io.tomaszstr.simplestockmarket.bank.BankService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletInventoryRepository inventoryRepository;
    private final BankService bankService;
    private final AuditService auditService;

    @Transactional
    public void performAction(UUID walletId, String ticker, ActionType actionType) {
        log.info("Processing {} request for wallet {} and stock {}", actionType, walletId, ticker);

        Wallet wallet = walletRepository.findById(walletId)
                .orElseGet(() -> {
                    log.debug("Wallet {} not found, creating new entry", walletId);
                    return walletRepository.save(new Wallet(walletId));
                });

        switch (actionType) {
            case BUY -> handleBuy(wallet, ticker);
            case SELL -> handleSell(wallet, ticker);
            default -> throw new IllegalArgumentException("Unknown action type: " + actionType);
        }

        auditService.recordSuccessfulOperation(walletId, ticker, actionType, 1L);
    }

    @Transactional(readOnly = true)
    public WalletStateResponse getWalletState(UUID walletId) {
        var wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletId));

        return WalletStateResponse.of(wallet);
    }

    @Transactional(readOnly = true)
    public long getStockQuantity(UUID walletId, String ticker) {
        return inventoryRepository.findById(new WalletInventory.WalletInventoryId(walletId, ticker))
                .map(WalletInventory::getQuantity)
                .orElse(0L);
    }

    private void handleBuy(Wallet wallet, String ticker) {
        bankService.reduceStock(ticker, 1L);

        var id = new WalletInventory.WalletInventoryId(wallet.getId(), ticker);
        var inventory = inventoryRepository.findById(id)
                .orElse(new WalletInventory(wallet, ticker, 0L));

        inventory.setQuantity(inventory.getQuantity() + 1);
        inventoryRepository.save(inventory);
        log.debug("Wallet {} bought 1 unit of {}", wallet.getId(), ticker);
    }

    private void handleSell(Wallet wallet, String ticker) {
        var id = new WalletInventory.WalletInventoryId(wallet.getId(), ticker);
        var inventory = inventoryRepository.findById(id)
                .filter(inv -> inv.getQuantity() > 0)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Insufficient holdings in wallet for: " + ticker));

        inventory.setQuantity(inventory.getQuantity() - 1);

        if (inventory.getQuantity() == 0) {
            inventoryRepository.delete(inventory);
        } else {
            inventoryRepository.save(inventory);
        }

        bankService.increaseStock(ticker, 1L);
        log.debug("Wallet {} sold 1 unit of {}", wallet.getId(), ticker);
    }
}
