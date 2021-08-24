package cz.dubcat.marketpoint.lang;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import cz.dubcat.marketpoint.MarketPoint;
import cz.dubcat.marketpoint.utils.ColorsUtil;

public class Translations {
    
    private static List<String> supportedLanguages = (List<String>) Arrays.asList("en");
    // Fallback english language
    private static File englishFile;
    private static FileConfiguration englishFileConfiguration;
    
    // Currently used language
    private static File loadedLangFile;
    private static FileConfiguration loadedLangFileConfiguration;
    
    private static String prefixColored;
    
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ColorsUtil.colorizeText(message));
    }
    
    public static void translateAndSend(CommandSender sender, String path) {
        sender.sendMessage(translateWithPrefix(path));
    }
    
    public static String translateWithPrefix(String path) {
        return prefixColored + translate(path);
    }
    
    public static String translate(String path) {
        String translation;

        if(loadedLangFileConfiguration.contains(path)) {
            translation = loadedLangFileConfiguration.getString(path);
        } else {
            translation = englishFileConfiguration.getString(path);
        }
        
        return ColorsUtil.colorizeText(translation);
    }
    
    public static void loadTranslations() {
        prepareLangFiles();
        String userLanguage = MarketPoint.getPlugin().getConfig().getString("settings.language", "en");

        englishFile = new File(MarketPoint.getPlugin().getDataFolder() + "/lang/en.yml");
        englishFileConfiguration = YamlConfiguration.loadConfiguration(englishFile);
        
        loadedLangFile = new File(MarketPoint.getPlugin().getDataFolder() + "/lang/"+userLanguage+".yml");
        if(loadedLangFile.exists()) {
            loadedLangFileConfiguration = YamlConfiguration.loadConfiguration(loadedLangFile);
        } else {
            loadedLangFileConfiguration = englishFileConfiguration;
        }
        
        updateLanguageDefaults();
        
        prefixColored = translate("lang.prefix");
    }
    
    private static void updateLanguageDefaults() {
        setLine("lang.prefix", "&f[&aMarketPoint&f] ", englishFileConfiguration);
        
        setLine("lang.menu.open", "&c/mp open <market> &f- Opens up market", englishFileConfiguration);
        setLine("lang.menu.create", "&c/mp create <market_id> &f- Creates a new market", englishFileConfiguration);
        setLine("lang.menu.delete", "&c/mp delete <market_id> &f- Deletes market", englishFileConfiguration);
        setLine("lang.menu.list", "&c/mp list &f- Lists all available markets", englishFileConfiguration);
        setLine("lang.menu.reload", "&c/mp reaload &f- Reloads plugin", englishFileConfiguration);
        
        setLine("lang.commandDoesNotExist", "&cCommand does not exist.", englishFileConfiguration);
        setLine("lang.marketDoesNotExist", "&cMarket &f%market_id% &cdoes not exist.", englishFileConfiguration);
        setLine("lang.itemAppearsToHaveNoPrice", "Oops! Item appears to have no set price.", englishFileConfiguration);
        setLine("lang.shiftClickTip", "Use &ashift-click &fto &ainsta-buy &fan item.", englishFileConfiguration);
        
        setLine("lang.market.nextPage", "&bNext Page ->", englishFileConfiguration);
        setLine("lang.market.previousPage", "&b<- Previous Page", englishFileConfiguration);
        setLine("lang.market.backToMarket", "&9Back to the market", englishFileConfiguration);
        setLine("lang.market.currencyInfo", "&aPrice of the item:", englishFileConfiguration);
        setLine("lang.currencyInfo", "Currency: %amount% %currency_name%", englishFileConfiguration);
        setLine("lang.customCurrencyInfo", "&5%currency_name%: &a%amount%", englishFileConfiguration);
        setLine("lang.noPermissionToOpen", "You can't open this market, because you dont have permission &a%permission%.", englishFileConfiguration);
        
        try {
            englishFileConfiguration.save(englishFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void prepareLangFiles() {
        supportedLanguages.forEach(lang -> {
            File languageFile = new File(MarketPoint.getPlugin().getDataFolder() + "/lang/"+lang+".yml");
            if (!languageFile.exists()) {
                languageFile.mkdirs();
                copyInputStreamToFile(MarketPoint.getPlugin().getClass().getResourceAsStream("/lang/"+lang+".yml"), 
                        new File(MarketPoint.getPlugin().getDataFolder() + "/lang/"+lang+".yml"));
            }
        });
    }
    
    private static void setLine(String key, Object content, FileConfiguration cfg) {
        if (!cfg.contains(key)) {
            cfg.set(key, content);
        }
    }
    
    private static void copyInputStreamToFile(InputStream stream, File targetFile) {
        try {
            Files.copy(stream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            stream.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
