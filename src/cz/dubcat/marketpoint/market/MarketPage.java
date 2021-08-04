package cz.dubcat.marketpoint.market;

import org.bukkit.configuration.file.FileConfiguration;

import cz.dubcat.marketpoint.MarketPoint;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = {"market"})
public class MarketPage {
    private String pageId;
    private Market market;
    private MarketDeal[] marketDeals = new MarketDeal[45];
    private String viewName;
     
    public MarketPage(Market market, String pageId) {
        this.market = market;
        this.pageId = pageId;
        this.loadMarketPage();
    }
    
    private void loadMarketPage() {
        FileConfiguration cfg = MarketPoint.getMarketDataManager().getMarketConfig(this.getMarket().getMarketId());
        
        if(cfg.contains("pages." + pageId + ".settings.viewName")) {
            this.viewName = cfg.getString("pages." + pageId + ".settings.viewName");
        }
        
        if(cfg.contains("pages." + pageId)) {
            for(String dealId : cfg.getConfigurationSection("pages." + pageId).getKeys(false)) {
                if(dealId.equals("settings")) {
                    continue;
                }
                marketDeals[Integer.valueOf(dealId)] = new MarketDeal(this, dealId);
            }
        }
    }
}
