package io.tomaszstr.simplestockmarket.wallet;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.tomaszstr.simplestockmarket.audit.AuditService;
import io.tomaszstr.simplestockmarket.bank.BankService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private WalletInventoryRepository inventoryRepository;
    @Mock
    private BankService bankService;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private WalletService walletService;

    @Test
    @DisplayName("Should create wallet and increment inventory on BUY")
    void givenNewWallet_whenPerformBuy_thenCreateAndSave() {
        // given
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId);
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());
        when(walletRepository.save(any())).thenReturn(wallet);
        when(inventoryRepository.findById(any())).thenReturn(Optional.empty());

        // when
        walletService.performAction(walletId, "AAPL", ActionType.BUY);

        // then
        verify(bankService).reduceStock("AAPL", 1L);
        verify(inventoryRepository).save(argThat(inv -> inv.getQuantity() == 1));
        verify(auditService).recordSuccessfulOperation(walletId, "AAPL", ActionType.BUY, 1L);
    }

    @Test
    @DisplayName("Should throw exception on SELL if inventory is missing")
    void givenNoInventory_whenPerformSell_thenThrowException() {
        // given
        UUID walletId = UUID.randomUUID();
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(new Wallet(walletId)));
        when(inventoryRepository.findById(any())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> walletService.performAction(walletId, "AAPL", ActionType.SELL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient holdings");
    }

    @Test
    @DisplayName("Should delete inventory record when quantity reaches zero on SELL")
    void givenOneItem_whenPerformSell_thenDeleteRecord() {
        // given
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId);
        WalletInventory inventory = new WalletInventory(wallet, "AAPL", 1L);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(inventoryRepository.findById(any())).thenReturn(Optional.of(inventory));

        // when
        walletService.performAction(walletId, "AAPL", ActionType.SELL);

        // then
        verify(inventoryRepository).delete(inventory);
        verify(bankService).increaseStock("AAPL", 1L);
    }

    @Test
    @DisplayName("Should decrement inventory but not delete if quantity > 1 on SELL")
    void givenMultipleItems_whenPerformSell_thenSaveDecrementedValue() {
        // given
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId);
        WalletInventory inventory = new WalletInventory(wallet, "AAPL", 10L);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(inventoryRepository.findById(any())).thenReturn(Optional.of(inventory));

        // when
        walletService.performAction(walletId, "AAPL", ActionType.SELL);

        // then
        verify(inventoryRepository).save(argThat(inv -> inv.getQuantity() == 9L));
        verify(inventoryRepository, never()).delete(any());
    }
}