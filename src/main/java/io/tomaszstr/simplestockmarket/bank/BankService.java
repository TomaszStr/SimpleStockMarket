package io.tomaszstr.simplestockmarket.bank;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
class BankService {

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
}
