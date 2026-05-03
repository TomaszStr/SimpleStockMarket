package io.tomaszstr.simplestockmarket.wallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private WalletService walletService;

    @Test
    @DisplayName("POST operation should return 200 and call service with parsed enum")
    void givenValidRequest_whenOperation_thenReturn200() {
        // given
        UUID walletId = UUID.randomUUID();
        String json = "{\"type\": \"buy\"}";

        // when & then
        mockMvc.post().uri("/wallets/{id}/stocks/AAPL", walletId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .assertThat().hasStatusOk();

        verify(walletService).performAction(walletId, "AAPL", ActionType.BUY);
    }

    @Test
    @DisplayName("GET wallet should return state DTO")
    void givenValidId_whenGetWallet_thenReturnWalletState() {
        // given
        UUID walletId = UUID.randomUUID();
        var response = new WalletStateResponse(walletId, List.of(new StockHoldings("AAPL", 10)));
        when(walletService.getWalletState(walletId)).thenReturn(response);

        // when & then
        mockMvc.get().uri("/wallets/{id}", walletId)
                .assertThat().hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.id",
                        id -> assertThat(id).asString().isEqualTo(walletId.toString()))
                .hasPathSatisfying("$.stocks[0].quantity",
                        q -> assertThat(q).asNumber().isEqualTo(10))
                .hasPathSatisfying("$.stocks[0].name",
                        n -> assertThat(n).asString().isEqualTo("AAPL"));
    }

    @Test
    @DisplayName("GET quantity should return numeric value")
    void givenValidParams_whenGetQuantity_thenReturnNumber() {
        // given
        UUID walletId = UUID.randomUUID();
        when(walletService.getStockQuantity(walletId, "AAPL")).thenReturn(5L);

        // when & then
        mockMvc.get().uri("/wallets/{id}/stocks/AAPL", walletId)
                .assertThat().hasStatusOk()
                .body().asString().isEqualTo("5");
    }
}