package cz.dubcat.marketpoint.utils;

import org.bukkit.ChatColor;

public class ColorsUtil {

    public static String colorizeText(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String stripColours(String string) {
        return ChatColor.stripColor(string);
    }
}
