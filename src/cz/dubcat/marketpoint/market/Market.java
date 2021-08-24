package cz.dubcat.marketpoint.market;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;

import cz.dubcat.marketpoint.MarketPoint;
import cz.dubcat.marketpoint.events.citizents.NpcClickListener;
import cz.dubcat.marketpoint.utils.ItemStackBase64;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = {"citizentsIds", "marketGuiName", "permissionToOpen"})
public class Market {
    private String marketId;
    private List<MarketPage> marketPages = new ArrayList<>();
    private String marketGuiName = "Market";
    private Set<Integer> citizentsIds = new HashSet<>();
    private String permissionToOpen = null;
    
    public Market(String marketId) {
        this.marketId = marketId;
        this.loadMarket();
    }
    
    public MarketPage getMarketPage(int page) {
        return this.marketPages.get(page);
    }
    
    @SuppressWarnings("unchecked")
    private void loadMarket() {
        FileConfiguration cfg = MarketPoint.getMarketDataManager().getMarketConfig(marketId);
        if(cfg.contains("pages")) {
            for(String pageId : cfg.getConfigurationSection("pages").getKeys(false)) {
                marketPages.add(new MarketPage(this, pageId));
            }
        }
        
        if(this.marketPages.size() == 0) {
            marketPages.add(new MarketPage(this, String.valueOf(0)));
        }
        
        if(cfg.contains("guiDisplayName")) {
            this.marketGuiName = cfg.getString("guiDisplayName");
        }
        
        if(cfg.contains("permissionToOpen")) {
            this.permissionToOpen = cfg.getString("permissionToOpen");
        }
        
        if(cfg.contains("citizentsIds")) {
            this.citizentsIds = new HashSet<>((List<Integer>) cfg.getList("citizentsIds"));
            this.citizentsIds.forEach(npcId -> { 
                NpcClickListener.NPC_TO_MARKET_MAP.put(npcId, this.marketId); 
            });
        }
    }
    
    public void saveMarket() {
        File marketFile = MarketPoint.getMarketDataManager().getMarketFile(marketId);
        if(marketFile.exists()) {
            marketFile.delete();
        }
        
        FileConfiguration cfg = MarketPoint.getMarketDataManager().getMarketConfig(marketId);
        cfg.set("guiDisplayName", this.marketGuiName);
        cfg.set("permissionToOpen", this.permissionToOpen);

        this.marketPages.forEach(page -> {
            if(page.getViewName() != null) {
                cfg.set("pages." + page.getPageId() + ".settings.viewName", page.getViewName());
            }
            
            for (int i = 0; i < page.getMarketDeals().length; i++) {
                MarketDeal deal = page.getMarketDeals()[i];
                if(deal != null) {
                    String dealPath = "pages." + page.getPageId() + "." + deal.getDealId() +".";
                    cfg.set(dealPath + "offer", ItemStackBase64.toBase64(deal.getOffer()));
                    cfg.set(dealPath + "contractCurrency", deal.getDealContract().getCurrencyCost());
                    cfg.set(dealPath + "contractItems", ItemStackBase64.convertItemStackListToBase64List(deal.getDealContract().getItems()));

                    if(!deal.getDealContract().getCustomCurrencies().isEmpty()) {
                        deal.getDealContract().getCustomCurrencies().forEach((type, currency) -> {
                            cfg.set(dealPath + "customCurrencies." + type, currency.getAmount()); 
                        });
                    }
                }
            }
        });
        
        if(!this.citizentsIds.isEmpty()) {
            cfg.set("citizentsIds", this.citizentsIds.toArray());
        }
        
        MarketPoint.getMarketDataManager().saveMarketConfig(this.marketId, cfg);
    }
}
