package cz.dubcat.marketpoint.events.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import cz.dubcat.marketpoint.api.MarketPointAPI;

public class MarketPlayerQuitEvent implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if(MarketPointAPI.PLAYER_MARKET_DATA.containsKey(e.getPlayer().getUniqueId())) {
            MarketPointAPI.PLAYER_MARKET_DATA.remove(e.getPlayer().getUniqueId());
        }
    }
}
