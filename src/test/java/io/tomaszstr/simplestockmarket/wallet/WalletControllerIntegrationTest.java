package io.tomaszstr.simplestockmarket.wallet;

import static org.assertj.core.api.Assertions.assertThat;

import io.tomaszstr.simplestockmarket.bank.BankInventory;
import io.tomaszstr.simplestockmarket.bank.BankInventoryRepository;
import io.tomaszstr.simplestockmarket.bank.Stock;
import io.tomaszstr.simplestockmarket.bank.StockRepository;
import io.tomaszstr.simplestockmarket.integration.BaseIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

class WalletControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletInventoryRepository walletInventoryRepository;

    @Autowired
    private BankInventoryRepository bankInventoryRepository;

    @Autowired
    private StockRepository stockRepository;

    private static final String TICKER = "AAPL";

    @BeforeEach
    void setUp() {
        walletInventoryRepository.deleteAllInBatch();
        walletRepository.deleteAllInBatch();
        bankInventoryRepository.deleteAllInBatch();
        stockRepository.deleteAllInBatch();

        Stock apple = stockRepository.save(new Stock(TICKER, "Apple Inc."));
        bankInventoryRepository.save(new BankInventory(apple, 100L));
    }

    @Test
    @DisplayName("givenNewWallet_whenBuyStock_thenCreateWalletAndSyncWithBank")
    void givenNewWallet_whenBuyStock_thenCreateWalletAndSyncWithBank() {
        // given
        UUID walletId = UUID.randomUUID();
        String buyJson = "{\"type\": \"buy\"}";

        // when
        mockMvc.post().uri("/wallets/{id}/stocks/{ticker}", walletId, TICKER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buyJson)
                .assertThat().hasStatusOk();

        // then
        assertThat(walletRepository.existsById(walletId)).isTrue();

        long walletQty = walletInventoryRepository.findAll().getFirst().getQuantity();
        long bankQty = bankInventoryRepository.findById(TICKER).orElseThrow().getQuantity();

        assertThat(walletQty).isEqualTo(1L);
        assertThat(bankQty).isEqualTo(99L);
    }

    @Test
    @DisplayName("givenEmptyBank_whenBuyStock_thenReturn400")
    void givenEmptyBank_whenBuyStock_thenReturn400() {
        // given
        UUID walletId = UUID.randomUUID();
        bankInventoryRepository.save(
                new BankInventory(stockRepository.getReferenceById(TICKER), 0L));
        String buyJson = "{\"type\": \"buy\"}";

        // when & then
        mockMvc.post().uri("/wallets/{id}/stocks/{ticker}", walletId, TICKER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buyJson)
                .assertThat().hasStatus(400);
    }

    @Test
    @DisplayName("givenNonExistentStock_whenBuyStock_thenReturn404")
    void givenNonExistentStock_whenBuyStock_thenReturn404() {
        // given
        UUID walletId = UUID.randomUUID();
        String buyJson = "{\"type\": \"buy\"}";

        // when & then
        mockMvc.post().uri("/wallets/{id}/stocks/NONEXISTENT", walletId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buyJson)
                .assertThat().hasStatus(404);
    }

    @Test
    @DisplayName("givenWalletWithStock_whenSellStock_thenUpdateBothBalances")
    void givenWalletWithStock_whenSellStock_thenUpdateBothBalances() {
        // given
        UUID walletId = UUID.randomUUID();
        mockMvc.post().uri("/wallets/{id}/stocks/{ticker}", walletId, TICKER)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\": \"buy\"}")
                .assertThat().hasStatusOk();

        String sellJson = "{\"type\": \"sell\"}";

        // when
        mockMvc.post().uri("/wallets/{id}/stocks/{ticker}", walletId, TICKER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(sellJson)
                .assertThat().hasStatusOk();

        // then
        assertThat(walletInventoryRepository.findAll()).isEmpty();
        assertThat(bankInventoryRepository.findById(TICKER).orElseThrow().getQuantity()).isEqualTo(
                100L);
    }

    @Test
    @DisplayName("givenNoStockInWallet_whenSellStock_thenReturn400")
    void givenNoStockInWallet_whenSellStock_thenReturn400() {
        // given
        UUID walletId = UUID.randomUUID();
        walletRepository.save(new Wallet(walletId));
        String sellJson = "{\"type\": \"sell\"}";

        // when & then
        mockMvc.post().uri("/wallets/{id}/stocks/{ticker}", walletId, TICKER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(sellJson)
                .assertThat().hasStatus(400);
    }

    @Test
    @DisplayName("givenExistingWallet_whenGetWallet_thenReturnHoldings")
    void givenExistingWallet_whenGetWallet_thenReturnHoldings() {
        // given
        UUID walletId = UUID.randomUUID();
        mockMvc.post().uri("/wallets/{id}/stocks/{ticker}", walletId, TICKER)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\": \"buy\"}")
                .assertThat().hasStatusOk();

        // when & then
        mockMvc.get().uri("/wallets/{id}", walletId)
                .assertThat().hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.id", id -> assertThat(id).asString().isEqualTo(walletId.toString()))
                .hasPathSatisfying("$.stocks[0].name", n -> assertThat(n).asString().isEqualTo(TICKER))
                .hasPathSatisfying("$.stocks[0].quantity", q -> assertThat(q).asNumber().isEqualTo(1));
    }

    @Test
    @DisplayName("givenExistingWallet_whenGetQuantity_thenReturnLongValue")
    void givenExistingWallet_whenGetQuantity_thenReturnLongValue() {
        // given
        UUID walletId = UUID.randomUUID();
        mockMvc.post().uri("/wallets/{id}/stocks/{ticker}", walletId, TICKER)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\": \"buy\"}")
                .assertThat().hasStatusOk();

        // when & then
        mockMvc.get().uri("/wallets/{id}/stocks/{ticker}", walletId, TICKER)
                .assertThat().hasStatusOk()
                .body().asString().isEqualTo("1");
    }
}
