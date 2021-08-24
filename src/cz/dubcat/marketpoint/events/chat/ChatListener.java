package cz.dubcat.marketpoint.events.chat;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;

import cz.dubcat.marketpoint.MarketPoint;
import cz.dubcat.marketpoint.api.MarketPointAPI;
import cz.dubcat.marketpoint.events.citizents.NpcClickListener;
import cz.dubcat.marketpoint.market.Market;
import cz.dubcat.marketpoint.market.MarketCurrency;
import cz.dubcat.marketpoint.market.MarketPage;
import cz.dubcat.marketpoint.market.data.PlayerData;
import cz.dubcat.marketpoint.utils.ColorsUtil;

public class ChatListener implements Listener {

    public static HashMap<UUID, ChatAction> CHAT_MAP = new HashMap<>();
    
    @EventHandler
    public void onChange(AsyncPlayerChatEvent e) {
        if(CHAT_MAP.containsKey(e.getPlayer().getUniqueId())) {
            ChatAction action = CHAT_MAP.get(e.getPlayer().getUniqueId());
            PlayerData data = MarketPointAPI.PLAYER_MARKET_DATA.get(e.getPlayer().getUniqueId());

            if(e.getMessage().equalsIgnoreCase("cancel")) {
                CHAT_MAP.remove(e.getPlayer().getUniqueId());
                Inventory marketEditorMain = MarketPointAPI.inst().getMarketGui(MarketPointAPI.LOADED_MARKETS.get(data.getMarketId()), data.getPage(), e.getPlayer(), true);

                Bukkit.getScheduler().runTask(MarketPoint.getPlugin(), () -> {
                    e.getPlayer().openInventory(marketEditorMain);
                });
                
                e.setCancelled(true);
            }
            
            if (action == ChatAction.PAGE_RENAME) {
                MarketPage page = MarketPointAPI.LOADED_MARKETS.get(data.getMarketId()).getMarketPage(data.getPage());
                page.setViewName(ColorsUtil.colorizeText(e.getMessage()));
                MarketPointAPI.inst().sendMessage(e.getPlayer(), "You have set page #" + data.getPage() + " name to " + ColorsUtil.colorizeText(e.getMessage()));
                Inventory marketEditorMain = MarketPointAPI.inst().getMarketGui(MarketPointAPI.LOADED_MARKETS.get(data.getMarketId()), data.getPage(), e.getPlayer(), true);
                Bukkit.getScheduler().runTask(MarketPoint.getPlugin(), () -> {
                    e.getPlayer().openInventory(marketEditorMain);
                });
                e.setCancelled(true);
            } else if(action == ChatAction.CITIZENTS_ID) {
                e.setCancelled(true);
                Market market = MarketPointAPI.LOADED_MARKETS.get(data.getMarketId());
                
                if(e.getMessage().startsWith("del")) {
                    Integer npcId = Integer.valueOf(e.getMessage().split(":")[1]);
                    market.getCitizentsIds().remove(npcId);
                    NpcClickListener.NPC_TO_MARKET_MAP.remove(npcId);
                    MarketPointAPI.inst().sendMessage(e.getPlayer(), "Market " + market.getMarketId() + " is now unassigned from NPC #" + npcId);
                    Inventory marketEditorMain = MarketPointAPI.inst().getMarketGui(MarketPointAPI.LOADED_MARKETS.get(data.getMarketId()), data.getPage(), e.getPlayer(), true);
                    Bukkit.getScheduler().runTask(MarketPoint.getPlugin(), () -> {
                        e.getPlayer().openInventory(marketEditorMain);
                    });
                } else {
                    Integer npcId = 0;
                    try {
                        npcId = Integer.valueOf(e.getMessage());
                        market.getCitizentsIds().add(npcId);
                        NpcClickListener.NPC_TO_MARKET_MAP.put(npcId, data.getMarketId());
                        MarketPointAPI.inst().sendMessage(e.getPlayer(), "Market " + market.getMarketId() + " is now assigned to NPC #" + npcId);
                        Inventory marketEditorMain = MarketPointAPI.inst().getMarketGui(MarketPointAPI.LOADED_MARKETS.get(data.getMarketId()), data.getPage(), e.getPlayer(), true);
                        Bukkit.getScheduler().runTask(MarketPoint.getPlugin(), () -> {
                            e.getPlayer().openInventory(marketEditorMain);
                        });
                    } catch (NumberFormatException ex) {
                        MarketPointAPI.inst().sendMessage(e.getPlayer(), e.getMessage() + " is not a number.");
                    }
                }
            } else if(action == ChatAction.CURRENCY_COST) {
                Market market = MarketPointAPI.LOADED_MARKETS.get(data.getMarketId());
                try {
                    double cost = Double.valueOf(e.getMessage());
                    market.getMarketPage(data.getPage()).getMarketDeals()[data.getCurrentDealId()].getDealContract().setCurrencyCost(cost);
                    MarketPointAPI.inst().sendMessage(e.getPlayer(), "Economy price has been set to " + cost);
                } catch(NumberFormatException ex) {
                    MarketPointAPI.inst().sendMessage(e.getPlayer(), e.getMessage() + " is not a number.");
                }
                e.setCancelled(true);
            } else if(action == ChatAction.CUSTOM_CURRENCY_CHOOSE_TYPE) {
               e.setCancelled(true);
               String currencyType = e.getMessage();
               if(MarketPointAPI.getCurrencyHandlers().containsKey(currencyType)) {
                   e.getPlayer().setMetadata("customType", new FixedMetadataValue(MarketPoint.getPlugin(), currencyType));
                   CHAT_MAP.put(e.getPlayer().getUniqueId(), ChatAction.CUSTOM_CURRENCY_SET_VALUE);
                   MarketPointAPI.inst().sendMessage(e.getPlayer(), "&aPlease write amount for &c" + currencyType + "&a:");
                   
                   return;
               } else {
                   MarketPointAPI.inst().sendMessage(e.getPlayer(), currencyType + " is not registered, choose something from " + MarketPointAPI.getCurrencyHandlers().keySet().toString());
               }
            } else if(action == ChatAction.CUSTOM_CURRENCY_SET_VALUE) {
                Market market = MarketPointAPI.LOADED_MARKETS.get(data.getMarketId());
                Double currencyValue = 0.0;
                String currencyType = e.getPlayer().getMetadata("customType").get(0).asString();
                try {
                    currencyValue = Double.valueOf(e.getMessage());
                    market.getMarketPage(data.getPage()).getMarketDeals()[data.getCurrentDealId()].getDealContract()
                        .getCustomCurrencies().put(currencyType, new MarketCurrency(currencyType, currencyValue));
                    MarketPointAPI.inst().sendMessage(e.getPlayer(), "Market " + market.getMarketId() + " has set custom currency " + currencyType + " to " + currencyValue);
                    Inventory marketEditorMain = MarketPointAPI.inst().getMarketGui(MarketPointAPI.LOADED_MARKETS.get(data.getMarketId()), data.getPage(), e.getPlayer(), true);
                    Bukkit.getScheduler().runTask(MarketPoint.getPlugin(), () -> {
                        e.getPlayer().openInventory(marketEditorMain);
                    });
                } catch (NumberFormatException ex) {
                    MarketPointAPI.inst().sendMessage(e.getPlayer(), e.getMessage() + " is not a number.");
                }
                e.setCancelled(true);
            } else if(action == ChatAction.PERMISSION_CHANGE) {
                Market market = MarketPointAPI.LOADED_MARKETS.get(data.getMarketId());
                String permission = e.getMessage();
                market.setPermissionToOpen(permission);
                MarketPointAPI.inst().sendMessage(e.getPlayer(),"You've succuessfuly set &7"+ e.getMessage() + " &fto be a required permission to open market " + market.getMarketId());
                e.setCancelled(true);
                Inventory marketEditorMain = MarketPointAPI.inst().getMarketGui(MarketPointAPI.LOADED_MARKETS.get(data.getMarketId()), data.getPage(), e.getPlayer(), true);
                Bukkit.getScheduler().runTask(MarketPoint.getPlugin(), () -> {
                    e.getPlayer().openInventory(marketEditorMain);
                });
            }
            
            CHAT_MAP.remove(e.getPlayer().getUniqueId());
        }
    }
}
