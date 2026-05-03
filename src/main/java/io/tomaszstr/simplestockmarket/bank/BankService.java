package io.tomaszstr.simplestockmarket.bank;

import io.tomaszstr.simplestockmarket.exception.StockNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankService {

    private final BankInventoryRepository inventoryRepository;
    private final StockRepository stockRepository;

    @Transactional(readOnly = true)
    BankStateResponse getBankState() {
        log.debug("Fetching current bank state");

        var stocks = inventoryRepository.findAll().stream()
                .map(inv -> new StockInfo(inv.getTicker(), inv.getQuantity())).toList();

        return new BankStateResponse(stocks);
    }

    @Transactional
    void setBankState(BankStateRequest request) {
        log.info("Setting bank state for {} distinct stocks", request.stocks().size());

        inventoryRepository.deleteAllInBatch();

        var inventories = request.stocks().stream().map(dto -> {
            Stock stock = stockRepository.findById(dto.name())
                    .orElseGet(() -> stockRepository.save(new Stock(dto.name(), dto.name())));

            return new BankInventory(stock, dto.quantity());
        }).toList();

        inventoryRepository.saveAll(inventories);
        log.debug("Bank state successfully updated");
    }

    /**
     * Reduces bank liquidity during a "BUY" operation.
     * Throws 404 if stock doesn't exist, 400 if bank is out of stock.
     */
    @Transactional
    public void reduceStock(String ticker, long quantity) {
        BankInventory inventory = inventoryRepository.findById(ticker)
                .orElseThrow(() -> new StockNotFoundException(
                        "Stock " + ticker + " does not exist in the system"));

        if (inventory.getQuantity() < quantity) {
            log.warn("Bank liquidity exhausted for {}. Requested: {}, Available: {}", ticker,
                    quantity, inventory.getQuantity());
            throw new IllegalArgumentException("Insufficient stock in the bank for: " + ticker);
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventoryRepository.save(inventory);
        log.info("Bank reduced {} stock by {}", ticker, quantity);
    }

    /**
     * Increases bank liquidity during a "SELL" operation.
     */
    @Transactional
    public void increaseStock(String ticker, long quantity) {
        BankInventory inventory = inventoryRepository.findById(ticker)
                .orElseThrow(() -> new StockNotFoundException(
                        "Stock " + ticker + " does not exist in the system"));

        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventoryRepository.save(inventory);
        log.info("Bank increased {} stock by {}", ticker, quantity);
    }
}
