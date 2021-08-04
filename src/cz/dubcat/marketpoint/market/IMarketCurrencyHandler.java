package cz.dubcat.marketpoint.market;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public interface IMarketCurrencyHandler {
    String getType();
    String getName();
    Plugin getPlugin();
    boolean playerHasAmount(Player player, double amount);
    boolean subtractAmountFromPlayer(Player player, double amount);
}
