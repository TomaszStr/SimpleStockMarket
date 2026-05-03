package io.tomaszstr.simplestockmarket.bank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BankControllerTest {

    @Mock
    private BankService bankService;

    @InjectMocks
    private BankController bankController;

    private static final String TICKER = "AAPL";
    private static final long QUANTITY = 500L;

    @Test
    @DisplayName("Should return 200 OK with bank state")
    void givenValidState_whenGetBankState_thenReturnOkResponse() {
        // given
        var expectedResponse = new BankStateResponse(List.of(new StockInfo(TICKER, QUANTITY)));
        given(bankService.getBankState()).willReturn(expectedResponse);

        // when
        var result = bankController.getBankState();

        // then
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("Should return 200 OK after setting bank state")
    void givenValidRequest_whenSetBankState_thenReturnOkStatus() {
        // given
        var request = new BankStateRequest(List.of(new StockInfo(TICKER, QUANTITY)));

        // when
        var result = bankController.setBankState(request);

        // then
        verify(bankService).setBankState(request);
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
    }
}