package cz.dubcat.marketpoint.market;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class MarketCurrency {
    private String type;
    private double amount;
}
