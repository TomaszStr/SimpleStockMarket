package io.tomaszstr.simplestockmarket.bank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {

    @Mock
    private BankInventoryRepository inventoryRepository;
    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private BankService bankService;

    private static final String TICKER_AAPL = "AAPL";
    private static final String TICKER_MSFT = "MSFT";
    private static final Stock STOCK_AAPL = new Stock(TICKER_AAPL, TICKER_AAPL);
    private static final Stock STOCK_MSFT = new Stock(TICKER_MSFT, TICKER_MSFT);
    private static final long QTY_100 = 100L;
    private static final long QTY_50 = 50L;

    @Test
    @DisplayName("Should return bank state mapped from inventory")
    void givenExistingInventories_whenGetBankState_thenReturnMappedResponse() {
        // given
        var inventory = List.of(
                new BankInventory(STOCK_AAPL, QTY_100),
                new BankInventory(STOCK_MSFT, QTY_50)
        );
        given(inventoryRepository.findAll()).willReturn(inventory);

        // when
        var response = bankService.getBankState();

        // then
        assertThat(response.stocks()).hasSize(2);
        assertThat(response.stocks()).extracting(StockInfo::name)
                .containsExactlyInAnyOrder(TICKER_AAPL, TICKER_MSFT);
    }

    @Test
    @DisplayName("Should register new stock if it does not exist in master catalog")
    void givenNewStockTicker_whenSetBankState_thenRegisterNewStockInMasterCatalog() {
        // given
        var request = new BankStateRequest(List.of(new StockInfo(TICKER_AAPL, QTY_100)));
        given(stockRepository.findById(TICKER_AAPL)).willReturn(Optional.empty());
        given(stockRepository.save(any(Stock.class)))
                .will(invocation -> invocation.getArgument(0));

        // when
        bankService.setBankState(request);

        // then
        verify(stockRepository).save(any(Stock.class));
        verify(inventoryRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should skip stock registration if ticker already exists")
    void givenExistingStockTicker_whenSetBankState_thenOnlyUpdateInventory() {
        // given
        var request = new BankStateRequest(List.of(new StockInfo(TICKER_AAPL, QTY_100)));
        given(stockRepository.findById(TICKER_AAPL)).willReturn(Optional.of(STOCK_AAPL));

        // when
        bankService.setBankState(request);

        // then
        verify(stockRepository, never()).save(any(Stock.class));
        verify(inventoryRepository).saveAll(anyList());
    }
}