package cz.dubcat.marketpoint.market.data;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MarketInventoryHolder implements InventoryHolder {
    
    private CurrentGui currentGui;
    
    @Override
    public Inventory getInventory() {
        return null;
    }

}
