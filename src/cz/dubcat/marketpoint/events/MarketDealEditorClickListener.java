package cz.dubcat.marketpoint.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import cz.dubcat.marketpoint.MarketPoint;
import cz.dubcat.marketpoint.api.MarketPointAPI;
import cz.dubcat.marketpoint.events.chat.ChatAction;
import cz.dubcat.marketpoint.events.chat.ChatListener;
import cz.dubcat.marketpoint.market.Market;
import cz.dubcat.marketpoint.market.MarketDeal;
import cz.dubcat.marketpoint.market.data.CurrentGui;
import cz.dubcat.marketpoint.market.data.MarketInventoryHolder;
import cz.dubcat.marketpoint.market.data.PlayerData;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class MarketDealEditorClickListener implements Listener {
    
    public static String MARKET_DEAL_EDITOR_INVENTORY_NAME = "Market Deal Editor";
    
    @EventHandler(ignoreCancelled = true)
    public void onMarketDealEditorClick(InventoryClickEvent e) {
        if(!(e.getWhoClicked() instanceof Player)) {
            return;
        }
        int slot = e.getRawSlot();
        Player player = (Player) e.getWhoClicked();
        if(e.getInventory().getHolder() instanceof MarketInventoryHolder) {
            MarketInventoryHolder holder = (MarketInventoryHolder) e.getInventory().getHolder();

            if(holder.getCurrentGui() != CurrentGui.DEAL_EDITOR) {
                return;
            }
            
            PlayerData data = MarketPointAPI.PLAYER_MARKET_DATA.get(player.getUniqueId());
            if(data != null && data.isEditor()) {
                Market market = MarketPointAPI.LOADED_MARKETS.get(data.getMarketId());
                int dealId = data.getCurrentDealId();
                int page = data.getPage();
                InventoryAction action = e.getAction();
                if(market == null) {
                    MarketPoint.getLog().warning("Trying to edit deal with null market. Market id " + data.getMarketId());
                    return;
                }
                
                if(MarketPointAPI.getAllowedDealSlots().contains(slot)) {
                    if(action == InventoryAction.PLACE_ALL) {
                        ItemStack item = e.getCursor();
                        if(item != null && Material.AIR != item.getType()) {
                            MarketDeal deal = market.getMarketPage(page).getMarketDeals()[dealId];
                            deal.getDealContract().getItems().add(item.clone());
                            player.openInventory(MarketPointAPI.inst().getDealEditorGui(market, dealId, page));
                            MarketPointAPI.inst().sendMessage(player, "You have added an item to the contract.");
                            player.getInventory().addItem(item.clone());
                            item.setAmount(0);
                            e.setCancelled(true);
                        }
                    } else if(action == InventoryAction.PICKUP_ALL) {
                        ItemStack item = e.getCurrentItem();
                        if(item != null && Material.AIR != item.getType()) {
                            e.setCancelled(true);
                            MarketDeal deal = market.getMarketPage(page).getMarketDeals()[dealId];
                            deal.getDealContract().getItems().remove(item);
                            player.openInventory(MarketPointAPI.inst().getDealEditorGui(market, dealId, page));
                            MarketPointAPI.inst().sendMessage(player, "You have removed an item from the contract.");
                        }
                    }
                } else if (slot == 37) {
                    e.setCancelled(true);
                    Inventory marketEditorMain = MarketPointAPI.inst().getMarketGui(market, page, player, true);
                    player.openInventory(marketEditorMain);
                } else if(slot == 43) {
                    market.getMarketPage(page).getMarketDeals()[data.getCurrentDealId()] = null;
                    e.setCancelled(true);
                    Inventory marketEditorMain = MarketPointAPI.inst().getMarketGui(market, page, player, true);
                    player.openInventory(marketEditorMain);
                } else if(slot == 42) {
                    //normal currency edit
                    e.setCancelled(true);
                    if(MarketPoint.getEconomy() != null) {
                        ChatListener.CHAT_MAP.put(player.getUniqueId(), ChatAction.CURRENCY_COST);
                        player.closeInventory();
                        MarketPointAPI.inst().sendMessage(player, "&aPlease write cost (number) of the deal:");
                    }
                } else if(slot == 41) {
                    //custom currency edit
                    if(!MarketPointAPI.getCurrencyHandlers().isEmpty()) {
                        ChatListener.CHAT_MAP.put(player.getUniqueId(), ChatAction.CUSTOM_CURRENCY_CHOOSE_TYPE);
                        player.closeInventory();
                        MarketPointAPI.inst().sendMessage(player, "&aPlease choose &7(click) a currency you want to edit:");
                        MarketPointAPI.getCurrencyHandlers().forEach((type, currency) -> {
                            
                            BaseComponent[] bc = new ComponentBuilder(" " + type + ":" + currency.getName())
                                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, type))
                                    .create();

                            player.spigot().sendMessage(bc);
                        });
                    } else {
                        e.setCancelled(true);
                    }
                } else if(slot < e.getInventory().getSize()){
                    e.setCancelled(true);
                }
            }
        }
    }
}
