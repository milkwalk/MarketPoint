package cz.dubcat.marketpoint.market;

import org.bukkit.inventory.ItemStack;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class HashedItem {
    private ItemStack item;
    private int amount = 0;
    
    public void increaseAmount() {
        this.amount++;
    }
}
