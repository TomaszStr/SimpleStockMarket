package io.tomaszstr.simplestockmarket.bank;

import static org.assertj.core.api.Assertions.assertThat;

import io.tomaszstr.simplestockmarket.integration.BaseIntegrationTest;
import java.util.List;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class BankIntegrationTest extends BaseIntegrationTest {

    private static final String ENDPOINT = "/stocks";
    private static final String TICKER_AAPL = "AAPL";
    private static final String TICKER_MSFT = "MSFT";
    private static final Stock STOCK_AAPL = new Stock(TICKER_AAPL, TICKER_AAPL);
    private static final Stock STOCK_MSFT = new Stock(TICKER_MSFT, TICKER_MSFT);
    private static final long QTY_100 = 100L;
    private static final long QTY_50 = 50L;

    @Autowired
    private BankInventoryRepository inventoryRepository;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    void cleanDb() {
        inventoryRepository.deleteAll();
        stockRepository.deleteAll();
    }

    @Test
    @DisplayName("givenInventoryExists_whenGetStocks_thenReturnCurrentBankState")
    void givenInventoryExists_whenGetStocks_thenReturnCurrentBankState() {
        // given
        stockRepository.saveAll(List.of(STOCK_AAPL, STOCK_MSFT));
        inventoryRepository.saveAll(List.of(
                new BankInventory(STOCK_AAPL, QTY_100),
                new BankInventory(STOCK_MSFT, QTY_50)
        ));

        // when & then
        mockMvc.get().uri(ENDPOINT)
                .assertThat().hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.stocks",
                        stocks -> assertThat(stocks).asInstanceOf(InstanceOfAssertFactories.LIST)
                                .hasSize(2))
                .hasPathSatisfying("$.stocks[?(@.name=='AAPL')].quantity",
                        q -> assertThat(q).asInstanceOf(InstanceOfAssertFactories.LIST)
                                .contains(100))
                .hasPathSatisfying("$.stocks[?(@.name=='MSFT')].quantity",
                        q -> assertThat(q).asInstanceOf(InstanceOfAssertFactories.LIST)
                                .contains(50));
    }

    @Test
    @DisplayName("givenValidRequest_whenPostStocks_thenUpdateDatabaseAndReturn200")
    void givenValidRequest_whenPostStocks_thenUpdateDatabaseAndReturn200() {
        // given
        var request = new BankStateRequest(List.of(new StockInfo(TICKER_AAPL, QTY_100)));
        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        mockMvc.post().uri(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .assertThat().hasStatusOk();

        // then
        var inventory = inventoryRepository.findById(TICKER_AAPL);
        assertThat(inventory).isPresent();
        assertThat(inventory.get().getQuantity()).isEqualTo(QTY_100);

        var stockMaster = stockRepository.findById(TICKER_AAPL);
        assertThat(stockMaster).isPresent();
    }

    @Test
    @DisplayName("givenNegativeQuantity_whenPostStocks_thenReturn400AndAggregatedErrors")
    void givenNegativeQuantity_whenPostStocks_thenReturn400AndAggregatedErrors() throws Exception {
        String validationErrorJson =
                """
                {
                  "stocks": [
                    { "name": "AAPL", "quantity": -10 },
                    { "name": "", "quantity": 50 }
                  ]
                }
                """;

        mockMvc.post().uri(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validationErrorJson)
                .assertThat().hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("['stocks[0].quantity']",
                        err -> assertThat(err).asString().contains("negative"));
    }

    @Test
    @DisplayName("givenMalformedJson_whenPostStocks_thenReturn400ViaGlobalHandler")
    void givenMalformedJson_whenPostStocks_thenReturn400ViaGlobalHandler() {
        // given
        String malformedJson =
                "{ \"stocks\": [ { \"name\": \"AAPL\", \"quantity\": \"not-a-number\" } ] }";

        // when & then
        mockMvc.post().uri(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson)
                .assertThat().hasStatus(HttpStatus.BAD_REQUEST);
    }
}