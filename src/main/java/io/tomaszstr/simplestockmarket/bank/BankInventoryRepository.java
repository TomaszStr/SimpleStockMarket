package io.tomaszstr.simplestockmarket.bank;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface BankInventoryRepository extends JpaRepository<BankInventory, String> {
}
