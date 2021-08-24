package cz.dubcat.marketpoint;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import cz.dubcat.marketpoint.api.ButtonProvider;
import cz.dubcat.marketpoint.api.MarketPointAPI;
import cz.dubcat.marketpoint.commands.CommandHandler;
import cz.dubcat.marketpoint.commands.cmd.CreteMarketCommand;
import cz.dubcat.marketpoint.commands.cmd.DeleteMarketCommand;
import cz.dubcat.marketpoint.commands.cmd.MarketListCommand;
import cz.dubcat.marketpoint.commands.cmd.MarketMenuCommand;
import cz.dubcat.marketpoint.commands.cmd.OpenMarketCommand;
import cz.dubcat.marketpoint.commands.cmd.ReloadMarketCommand;
import cz.dubcat.marketpoint.events.MarketClickListener;
import cz.dubcat.marketpoint.events.MarketDealClickListener;
import cz.dubcat.marketpoint.events.MarketDealEditorClickListener;
import cz.dubcat.marketpoint.events.MarketEditorClickListener;
import cz.dubcat.marketpoint.events.MarketInventoryCloseEvent;
import cz.dubcat.marketpoint.events.chat.ChatListener;
import cz.dubcat.marketpoint.events.citizents.NpcClickListener;
import cz.dubcat.marketpoint.events.player.MarketPlayerQuitEvent;
import cz.dubcat.marketpoint.lang.Translations;
import cz.dubcat.marketpoint.market.Market;
import cz.dubcat.marketpoint.utils.MarketDataManager;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;

public class MarketPoint extends JavaPlugin{

    public static Random r = new Random();
    @Getter
    private static Logger log;
    @Getter
    private static MarketPoint plugin;
    private static MarketDataManager dataManager;
    @Getter
    private static boolean citizentsLoaded = false;
    @Getter
    private static Economy economy = null;
    @Getter
    private static Logger purchaseLogger = Logger.getLogger("PurchaseLog");
    private FileHandler fh;
    
    @Override
    public void onEnable() {
        plugin = this;
        log = getLogger();
        Translations.loadTranslations();
        dataManager = new MarketDataManager(this);
        setupEconomy();
        setUpLogs();
        
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        registerCommands();
        loadMarkets();
        ButtonProvider.initializeButtons();
        
        getServer().getPluginManager().registerEvents(new MarketEditorClickListener(), this);
        getServer().getPluginManager().registerEvents(new MarketClickListener(), this);
        getServer().getPluginManager().registerEvents(new MarketDealEditorClickListener(), this);
        getServer().getPluginManager().registerEvents(new MarketDealClickListener(), this);
        getServer().getPluginManager().registerEvents(new MarketInventoryCloseEvent(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new MarketPlayerQuitEvent(), this);
        
        if(Bukkit.getPluginManager().getPlugin("Citizens") != null) {
            ButtonProvider.loadCitizetndsButtons();
            getServer().getPluginManager().registerEvents(new NpcClickListener(), this);
            citizentsLoaded = true;
            log.info("Citizents module loaded.");
        }
        this.registerMetrics();

        log.info("Enabled.");
    }
    
    @Override
    public void onDisable() {
        fh.close();
        MarketPointAPI.LOADED_MARKETS.forEach((id, market) -> {
            market.saveMarket();
        });
        log.info("Disabled.");
    }
    
    private void registerCommands() {
        CommandHandler handler = new CommandHandler("marketpoint");
        handler.registerMenu(new MarketMenuCommand());
        handler.register("open", new OpenMarketCommand()); 
        handler.register("create", new CreteMarketCommand()); 
        handler.register("delete", new DeleteMarketCommand()); 
        handler.register("reload", new ReloadMarketCommand());
        handler.register("list", new MarketListCommand());
        
        getCommand("marketpoint").setExecutor(handler);
    }
    
    private void setUpLogs() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        File logPath = new File(this.getDataFolder() + "/logs/" + simpleDateFormat.format(new Date()));
        
        if(!logPath.exists()) {
            logPath.mkdirs();
        }
        
        try {
            fh = new FileHandler(logPath.getPath() + "/purchase-log.log", true);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
        purchaseLogger.addHandler(fh);
    }
    
    private void loadMarkets() {
        File files = new File(this.getDataFolder() + "/markets");
        if(files != null && files.listFiles() != null) {
            for(File f: files.listFiles()) {
                String marketName = f.getName().replaceAll(".yml", "");
                try {
                    MarketPointAPI.LOADED_MARKETS.put(marketName, new Market(marketName));
                } catch (Exception e) {
                    e.printStackTrace();
                    f.delete();
                    log.warning("Market " + marketName +" has been deleted! Could not load market.");
                }
            }
        }
        
        log.info("Loaded " + MarketPointAPI.LOADED_MARKETS.size() + " market points.");
    }
    
    private void registerMetrics() {
        Metrics metrics = new Metrics(this, 12325);

        metrics.addCustomChart(new SimplePie("citizents", () -> {
            return String.valueOf(MarketPoint.citizentsLoaded);
        }));
        
        metrics.addCustomChart(new SimplePie("vault_economy", () -> {
            return MarketPoint.economy != null ? "true" : "false";
        }));
        
        metrics.addCustomChart(new SimplePie("currency_handler", () -> {
            return MarketPointAPI.getCurrencyHandlers().isEmpty() ? "false" : "true";
        }));
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();

        return economy != null;
    }
    
    public static MarketDataManager getMarketDataManager() { 
        return dataManager;
    }
}
