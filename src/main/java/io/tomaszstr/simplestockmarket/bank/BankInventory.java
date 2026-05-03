package io.tomaszstr.simplestockmarket.bank;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bank_inventory")
@Getter
@Setter
@NoArgsConstructor
class BankInventory {
    @Id
    private String ticker;
    private Long quantity;
    @MapsId
    @ManyToOne
    @JoinColumn(name = "ticker")
    private Stock stock;

    public BankInventory(Stock stock, long quantity) {
        this.stock = stock;
        this.ticker = stock.getTicker();
        this.quantity = quantity;
    }
}
