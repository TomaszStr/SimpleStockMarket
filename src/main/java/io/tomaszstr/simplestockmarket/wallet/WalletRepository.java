package io.tomaszstr.simplestockmarket.wallet;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface WalletRepository extends JpaRepository<Wallet, UUID> {
}

