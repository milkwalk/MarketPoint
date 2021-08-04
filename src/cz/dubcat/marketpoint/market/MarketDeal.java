package cz.dubcat.marketpoint.market;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import cz.dubcat.marketpoint.MarketPoint;
import cz.dubcat.marketpoint.utils.ItemStackBase64;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = {"marketPage"})
public class MarketDeal {
    private String dealId;
    private MarketPage marketPage;
    private ItemStack offer;
    private DealContract dealContract;
    
    public MarketDeal(MarketPage marketPage, String dealId){
        this.marketPage = marketPage;
        this.dealId = dealId;
        this.dealContract = new DealContract();
        this.loadMarketDeal();
    }
    
    private void loadMarketDeal() {
        FileConfiguration cfg = MarketPoint.getMarketDataManager().getMarketConfig(this.getMarketPage().getMarket().getMarketId());
        String path = "pages." + this.getMarketPage().getPageId() + "." + dealId + ".";
        if(cfg.contains("pages." + this.getMarketPage().getPageId() + "." + dealId)) {
            try {
                this.offer = ItemStackBase64.fromBase64(cfg.getString(path + "offer"));
                this.dealContract.setCurrencyCost(cfg.getDouble(path + "contractCurrency"));
                this.getDealContract().getItems().addAll(ItemStackBase64.convertBase64ListToItemStackList(cfg.getStringList(path + "contractItems")));
                
                Map<String, MarketCurrency> customCurrencies = new HashMap<>();
                if(cfg.contains(path + "customCurrencies")) {
                    cfg.getConfigurationSection(path + "customCurrencies")
                    .getKeys(false).forEach(customCurrency -> {
                    customCurrencies.put(customCurrency, new MarketCurrency(customCurrency, cfg.getDouble(path + "customCurrencies." + customCurrency)));
                });
                }
                this.dealContract.setCustomCurrencies(customCurrencies);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
