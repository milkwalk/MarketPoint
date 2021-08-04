package cz.dubcat.marketpoint.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import cz.dubcat.marketpoint.api.MarketPointAPI;
import cz.dubcat.marketpoint.market.data.CurrentGui;
import cz.dubcat.marketpoint.market.data.MarketInventoryHolder;
import cz.dubcat.marketpoint.market.data.PlayerData;

public class MarketInventoryCloseEvent implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMarketClose(InventoryCloseEvent e) {
        if(MarketPointAPI.PLAYER_MARKET_DATA.containsKey(e.getPlayer().getUniqueId())) {
            PlayerData data = MarketPointAPI.PLAYER_MARKET_DATA.get(e.getPlayer().getUniqueId());
            
            if(e.getInventory().getHolder() instanceof MarketInventoryHolder) {
                MarketInventoryHolder holder = (MarketInventoryHolder) e.getInventory().getHolder();
                if(holder.getCurrentGui() == CurrentGui.DEAL) {
                    MarketPointAPI.getAllowedDealSlots().forEach(slot -> {
                        ItemStack item = e.getInventory().getItem(slot);
                        if(item != null && item.getType() != Material.AIR) {
                            e.getPlayer().getInventory().addItem(item);
                        }
                    });
                    ((Player) e.getPlayer()).updateInventory();
                }
            }
            
            if(data.isEditor()) {
                MarketPointAPI.LOADED_MARKETS.get(data.getMarketId()).saveMarket();
            }
        }
    }
}
