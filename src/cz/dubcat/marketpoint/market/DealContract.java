package cz.dubcat.marketpoint.market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.inventory.ItemStack;

import cz.dubcat.marketpoint.api.MarketPointAPI;
import lombok.Data;

@Data
public class DealContract {
    //add different checks options
    private List<ItemStack> items = new ArrayList<>();
    private double currencyCost = 0;
    private Map<String, MarketCurrency> customCurrencies = new HashMap<>();
    
    
    public List<MarketCurrency> getFilteredCustomCurrencies() {
        return getCustomCurrencies().values().stream()
            .filter(currency -> MarketPointAPI.getCurrencyHandlers().containsKey(currency.getType()))
            .filter(currency -> currency.getAmount() > 0)
            .collect(Collectors.toList());
    }
    
    public boolean isEmpty() {
        return currencyCost == 0 && items.isEmpty() && customCurrencies.isEmpty();
    }
}
