package io.tomaszstr.simplestockmarket.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface WalletInventoryRepository
        extends JpaRepository<WalletInventory, WalletInventory.WalletInventoryId> {
}
