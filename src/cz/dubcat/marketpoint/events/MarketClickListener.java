package cz.dubcat.marketpoint.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import cz.dubcat.marketpoint.api.ButtonProvider;
import cz.dubcat.marketpoint.api.MarketPointAPI;
import cz.dubcat.marketpoint.market.Market;
import cz.dubcat.marketpoint.market.data.CurrentGui;
import cz.dubcat.marketpoint.market.data.MarketInventoryHolder;
import cz.dubcat.marketpoint.market.data.PlayerData;

public class MarketClickListener implements Listener {
    
    private static final int EDITOR_MODE_ID = 49;
    private static final int NEXT_PAGE_ID = 52;
    private static final int PREVIOUS_PAGE_ID = 46;
    
    @EventHandler(ignoreCancelled = true)
    public void onMarketClickEvent(InventoryClickEvent e) {
        if(e.getWhoClicked() instanceof Player) {
            Player player = (Player) e.getWhoClicked();
            int slot = e.getRawSlot();
            
            if(e.getInventory().getHolder() instanceof MarketInventoryHolder) {
                MarketInventoryHolder holder = (MarketInventoryHolder) e.getInventory().getHolder();
                
                if(holder.getCurrentGui() == CurrentGui.MAIN) {
                    try {
                        PlayerData playerData = MarketPointAPI.PLAYER_MARKET_DATA.get(player.getUniqueId());
                        InventoryAction action = e.getAction();
                        Market market = MarketPointAPI.LOADED_MARKETS.get(playerData.getMarketId());
                        if(action == InventoryAction.PICKUP_ALL && slot >= 0 && slot < e.getInventory().getSize() - 9) {
                            ItemStack item = e.getCurrentItem();
                            if(item != null && item.getType() != Material.AIR) {
                                Inventory dealInventory = MarketPointAPI.inst().getDealGui(market, e.getRawSlot(), playerData.getPage());
                                player.openInventory(dealInventory);
                                playerData.setCurrentDealId(e.getRawSlot());
                            }
                        } else if(slot == EDITOR_MODE_ID && player.hasPermission("marketpoint.admin")) {
                            playerData.setEditor(true);
                            Inventory dealInventory = MarketPointAPI.inst().getMarketGui(market, playerData.getPage(), player, true);
                            player.openInventory(dealInventory);
                        } else if(slot == NEXT_PAGE_ID && e.getCurrentItem() != null && e.getCurrentItem().equals(ButtonProvider.NEXT_PAGE_BUTTON)) {
                            playerData.setPage(playerData.getPage() + 1);
                            Inventory dealInventory = MarketPointAPI.inst().getMarketGui(market, playerData.getPage(), player, false);
                            player.openInventory(dealInventory);
                        } else if (slot == PREVIOUS_PAGE_ID && e.getCurrentItem() != null && e.getCurrentItem().equals(ButtonProvider.PREVIOUS_PAGE_BUTTON)) {
                            playerData.setPage(playerData.getPage() - 1);
                            Inventory dealInventory = MarketPointAPI.inst().getMarketGui(market, playerData.getPage(), player, false);
                            player.openInventory(dealInventory);
                        }
                        e.setCancelled(true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        e.setCancelled(true);
                        MarketPointAPI.PLAYER_MARKET_DATA.remove(player.getUniqueId());
                        player.closeInventory();
                    }
                }
            }
        }
    }
}
