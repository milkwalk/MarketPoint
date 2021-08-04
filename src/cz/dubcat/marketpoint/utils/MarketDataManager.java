package cz.dubcat.marketpoint.utils;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MarketDataManager {
    private JavaPlugin plugin;
    
    public FileConfiguration getMarketConfig(String player) {
        File playerYml = this.getMarketFile(player);
        return YamlConfiguration.loadConfiguration(playerYml);
    }
    
    public void saveMarketConfig(String marketId, FileConfiguration config) {
        File guildYml = this.getMarketFile(marketId);
        try {
            config.save(guildYml);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public File getMarketFile(String marketId) {
        return new File(plugin.getDataFolder() + "/markets/" + marketId + ".yml");
    }
}
