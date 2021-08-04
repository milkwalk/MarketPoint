package cz.dubcat.marketpoint.events.citizents;

import java.util.HashMap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import cz.dubcat.marketpoint.api.MarketPointAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;

public class NpcClickListener implements Listener {
    
    public static HashMap<Integer, String> NPC_TO_MARKET_MAP = new HashMap<>();
    
    @EventHandler(ignoreCancelled = true)
    public void onRightClick(NPCRightClickEvent event) {
        int npcId = event.getNPC().getId();
        
        if(NPC_TO_MARKET_MAP.containsKey(npcId)) {
            String marketId = NPC_TO_MARKET_MAP.get(npcId);
            MarketPointAPI.inst().openMarket(marketId, event.getClicker());
        }
    }
}
