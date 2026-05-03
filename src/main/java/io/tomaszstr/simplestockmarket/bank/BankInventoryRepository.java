package io.tomaszstr.simplestockmarket.bank;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankInventoryRepository extends JpaRepository<BankInventory, String> {
}
