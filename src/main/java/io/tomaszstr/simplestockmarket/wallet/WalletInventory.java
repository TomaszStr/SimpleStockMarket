package io.tomaszstr.simplestockmarket.wallet;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "wallet_inventory")
@Getter
@Setter
@NoArgsConstructor
public class WalletInventory {

    @EmbeddedId
    private WalletInventoryId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("walletId")
    private Wallet wallet;

    @Column(nullable = false)
    private Long quantity;

    public WalletInventory(Wallet wallet, String ticker, Long quantity) {
        this.wallet = wallet;
        this.id = new WalletInventoryId(wallet.getId(), ticker);
        this.quantity = quantity;
    }

    @Embeddable
    public record WalletInventoryId(UUID walletId, String ticker) implements Serializable {
    }
}
