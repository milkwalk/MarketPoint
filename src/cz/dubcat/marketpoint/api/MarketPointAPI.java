package cz.dubcat.marketpoint.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import cz.dubcat.marketpoint.MarketPoint;
import cz.dubcat.marketpoint.events.MarketDealEditorClickListener;
import cz.dubcat.marketpoint.lang.Translations;
import cz.dubcat.marketpoint.market.DealContract;
import cz.dubcat.marketpoint.market.HashedItem;
import cz.dubcat.marketpoint.market.IMarketCurrencyHandler;
import cz.dubcat.marketpoint.market.Market;
import cz.dubcat.marketpoint.market.MarketDeal;
import cz.dubcat.marketpoint.market.MarketPage;
import cz.dubcat.marketpoint.market.data.CurrentGui;
import cz.dubcat.marketpoint.market.data.MarketInventoryHolder;
import cz.dubcat.marketpoint.market.data.PlayerData;
import cz.dubcat.marketpoint.utils.ColorsUtil;
import lombok.Getter;

public class MarketPointAPI {
    
    public static final int MAX_PAGES_PER_MARKET = 15;
    public static Map<UUID, PlayerData> PLAYER_MARKET_DATA = new HashMap<>();
    public static Map<String, Market> LOADED_MARKETS = new HashMap<>();
    
    private static MarketPointAPI instance = null;
    private static LinkedHashSet<Integer> ALLOWED_DEAL_SLOTS = new LinkedHashSet<>(Arrays.asList(9,10,11,18,19,20,21,27,28,29,30));
    private static LinkedHashSet<Integer> DEALER_DEAL_SLOTS = new LinkedHashSet<>(Arrays.asList(15,16,17,23,24,25,26,31,32,33,34,35));
    private static Inventory DEFAULT_DEAL_INVENTORY;
    @Getter
    private static Map<String, IMarketCurrencyHandler> currencyHandlers = new HashMap<>();
    
    public static MarketPointAPI inst() { 
        if(instance == null) {
            instance = new MarketPointAPI();
            initalizeDealInventory();
        }
        
        return instance;
    }

    public void registerCurrencyHandler(IMarketCurrencyHandler handler) {
        currencyHandlers.put(handler.getType(), handler);
        MarketPoint.getLog().info("Registered " + handler.getType() + " currency handler from " + handler.getPlugin().getName() + ".");
    }
    
    public void openMarket(String marketId, Player player) {
        Market market = LOADED_MARKETS.get(marketId);
        
        if(market.getPermissionToOpen() != null && !player.hasPermission(market.getPermissionToOpen())) {
            String translation = Translations.translateWithPrefix("lang.noPermissionToOpen");
            translation = translation.replaceAll("%permission%", market.getPermissionToOpen());
            this.sendMessage(player, translation);

            return;
        }
        
        int page = 0;
        PlayerData data = PLAYER_MARKET_DATA.computeIfAbsent(player.getUniqueId(), key -> new PlayerData(marketId));
        if(data.getMarketId() != marketId) {
            PLAYER_MARKET_DATA.put(player.getUniqueId(), new PlayerData(marketId));
        } else {
            page = data.getPage();
        }

        Inventory maraketGui = getMarketGui(market, page, player, false);
        player.openInventory(maraketGui);
    }
    
    public Inventory getMarketGui(Market market, int page, Player target, boolean editorMode) {
        boolean admin = target.hasPermission("marketpoint.admin");
        
        if(page < 0) {
            page = 0;
        } else if(page > MAX_PAGES_PER_MARKET) {
            page = MAX_PAGES_PER_MARKET;
        }
        
        //check of page is larger than maxpages and add new one
        if(editorMode && page == market.getMarketPages().size()) {
            boolean lastPageHasItem = false;
            MarketDeal[] deals = market.getMarketPage(market.getMarketPages().size() - 1).getMarketDeals();
            for(int i =0; i < deals.length; i++) {
                MarketDeal marketDeal = deals[i];
                if(marketDeal != null) {
                    lastPageHasItem = true;
                    break;
                }
            }
            
            if(!lastPageHasItem) {
                return null;
            } else {
                market.getMarketPages().add(new MarketPage(market, String.valueOf(page)));
            }
        }
        
        String marketPageDisplay = ChatColor.RESET + " " + (page + 1) + "/" + market.getMarketPages().size();
        String marketViewName = market.getMarketPage(page).getViewName() == null ?
                market.getMarketGuiName() : 
                market.getMarketPage(page).getViewName();
        Inventory inventory = Bukkit.createInventory(new MarketInventoryHolder(editorMode ? CurrentGui.MAIN_EDITOR : CurrentGui.MAIN), 54, marketViewName + marketPageDisplay);
        
        if(page < market.getMarketPages().size()) {
            MarketPage marketPage = market.getMarketPage(page);
            for(int i =0; i < marketPage.getMarketDeals().length; i++) {
                MarketDeal marketDeal = marketPage.getMarketDeals()[i];
                if(marketDeal != null) {
                    inventory.setItem(i, marketDeal.getOffer());
                }
            }
        }
        
        for(int i = 53; i >= 45; i--) {
            switch (i) {
                case 46:
                    if(page > 0) {
                        inventory.setItem(i, ButtonProvider.PREVIOUS_PAGE_BUTTON);
                    } else {
                        inventory.setItem(i, ButtonProvider.MAIN_THEME_FILLER);
                    }
                    break;
                case 52:
                    if(market.getMarketPages().size() - 1 > page || (admin && editorMode)) {
                        inventory.setItem(i, ButtonProvider.NEXT_PAGE_BUTTON);
                    } else {
                        inventory.setItem(i, ButtonProvider.MAIN_THEME_FILLER);
                    }
                    break;
                case 49:
                    if(admin) {
                        if(editorMode) {
                            inventory.setItem(i, ButtonProvider.LEAVE_EDITOR_MODE_BUTTON);
                        } else {
                            inventory.setItem(i, ButtonProvider.ENTER_EDITOR_MODE_BUTTON);
                        }
                    } else {
                        inventory.setItem(i, ButtonProvider.MAIN_THEME_FILLER);
                    }
                    break;
                case 47:
                    if(editorMode) {
                        ItemStack permissionsButton = ButtonProvider.PERMISSIONS_BUTTON.clone();
                        ItemMeta meta = permissionsButton.getItemMeta();
                        List<String> lores = meta.getLore();
                        for (int j = 0; j < lores.size(); j++) {
                            String line = lores.get(j);
                            if(line.contains("%value%")) {
                                if(market.getPermissionToOpen() == null) {
                                    lores.set(j, line.replaceAll("%value%", "No permission is set"));
                                } else {
                                    lores.set(j, line.replaceAll("%value%", market.getPermissionToOpen()));
                                }
                            }
                        }
                        meta.setLore(lores);
                        permissionsButton.setItemMeta(meta);
                        inventory.setItem(i, permissionsButton);
                    } else {
                        inventory.setItem(i, ButtonProvider.MAIN_THEME_FILLER);
                    }
                    break;
                case 48: 
                    if(editorMode) {
                        inventory.setItem(i, ButtonProvider.RENAME_INVENTORY_BUTTON);
                    } else {
                        inventory.setItem(i, ButtonProvider.MAIN_THEME_FILLER);
                    }
                    break;
                case 50:
                    if(editorMode && MarketPoint.isCitizentsLoaded()) {
                        ItemStack citizensButton = ButtonProvider.CITIZENTS_BUTTON.clone();
                        ItemMeta meta = citizensButton.getItemMeta();
                        List<String> lores = meta.getLore();
                        for (int j = 0; j < lores.size(); j++) {
                            String line = lores.get(j);
                            if(line.contains("%value%")) {
                                List<String> citizents = market.getCitizentsIds().stream()
                                        .map(id -> String.valueOf(id))
                                        .collect(Collectors.toList());
                                lores.set(j, line.replaceAll("%value%", String.join(", ", citizents)));
                            }
                        }
                        meta.setLore(lores);
                        citizensButton.setItemMeta(meta);
                        inventory.setItem(i, citizensButton);
                    } else {
                        inventory.setItem(i, ButtonProvider.MAIN_THEME_FILLER);
                    }
                    break;
                default:
                    inventory.setItem(i, ButtonProvider.MAIN_THEME_FILLER);
                    break;
            }
        }
        
        return inventory;
    }
    
    public Inventory getDealGui(Market market, int dealId, int page) {
        MarketDeal deal = market.getMarketPages().get(page).getMarketDeals()[dealId];
        ItemStack offer = deal.getOffer();
        String displayName = (offer.hasItemMeta() && offer.getItemMeta().hasDisplayName()) ? offer.getItemMeta().getDisplayName() : offer.getType().name().replaceAll("_", " ");
        Inventory inventory = Bukkit.createInventory(new MarketInventoryHolder(CurrentGui.DEAL),45, displayName + (offer.getAmount() > 1 ? ChatColor.RESET + " x" + offer.getAmount() : ""));
        inventory.setContents(DEFAULT_DEAL_INVENTORY.getContents().clone());
        
        //other overrides
        inventory.setItem(4, deal.getOffer());
        
        DealContract contract = deal.getDealContract();
        boolean useVault = contract.getCurrencyCost() > 0 && MarketPoint.getEconomy() != null;
        
        if(useVault || !contract.getCustomCurrencies().isEmpty()) {
            ItemStack currencyInfo = ButtonProvider.CURRENCY_INFO.clone();
            List<String> lore = new ArrayList<>();
            if(useVault) {
                String translation = Translations.translate("lang.currencyInfo");
                translation = translation.replaceAll("%amount%", String.valueOf(contract.getCurrencyCost()));
                translation = translation.replaceAll("%currency_name%", MarketPoint.getEconomy().currencyNamePlural());
                lore.add(translation);
            }
            
            contract.getCustomCurrencies().values().stream()
                .filter(currency -> currencyHandlers.containsKey(currency.getType()))
                .filter(currency -> currency.getAmount() > 0)
                .forEach(currency -> {
                    String translation = Translations.translate("lang.customCurrencyInfo");
                    translation = translation.replaceAll("%amount%", String.valueOf(currency.getAmount()));
                    translation = translation.replaceAll("%currency_name%", currencyHandlers.get(currency.getType()).getName());
                    
                    lore.add(translation);
                });
            ItemMeta meta = currencyInfo.getItemMeta();
            meta.setLore(lore);
            currencyInfo.setItemMeta(meta);
            inventory.setItem(13, currencyInfo);
        }
        
        //populating items
        if(deal != null) {
            int i = 0;
            List<Integer> allowedSlots = new ArrayList<>(DEALER_DEAL_SLOTS);
            for(ItemStack dealItem: deal.getDealContract().getItems()) {
                inventory.setItem(allowedSlots.get(i), dealItem);
                i++;
            }
        }

        
        return inventory;
    }
    
    public Inventory getDealEditorGui(Market market, int dealId, int page) {
        MarketDeal deal = market.getMarketPages().get(page).getMarketDeals()[dealId];
        if(deal == null) {
            return null;
        }
        
        Inventory inventory = Bukkit.createInventory(new MarketInventoryHolder(CurrentGui.DEAL_EDITOR),45, 
                MarketDealEditorClickListener.MARKET_DEAL_EDITOR_INVENTORY_NAME);
        inventory.setContents(DEFAULT_DEAL_INVENTORY.getContents().clone());
        
        //other overrides
        inventory.setItem(43, ButtonProvider.DELETE_DEAL_BUTTON);
        if(MarketPoint.getEconomy() != null) {
            inventory.setItem(42, replacePlaceholdersInLore(ButtonProvider.CURRENCY_EDIT_BUTTON.clone(), 
                    "%current_currency_value%", deal.getDealContract().getCurrencyCost()));
        }
        ItemStack customCurrencyButton = ButtonProvider.CUSTOM_CURRENCY_EDIT_BUTTON.clone();
        if(!deal.getDealContract().getCustomCurrencies().isEmpty()) {
            List<String> lore = new ArrayList<>();
            deal.getDealContract().getCustomCurrencies().values().forEach(currency -> { 
                lore.add(currencyHandlers.get(currency.getType()).getName() + ": " + currency.getAmount());
            });
            ItemMeta meta = customCurrencyButton.getItemMeta();
            meta.setLore(lore);
            customCurrencyButton.setItemMeta(meta);
        }

        inventory.setItem(41, customCurrencyButton);
        inventory.setItem(4, deal.getOffer());
        
        //populating items
        if(deal != null) {
            int i = 0;
            List<Integer> allowedSlots = new ArrayList<>(ALLOWED_DEAL_SLOTS);
            for(ItemStack dealItem: deal.getDealContract().getItems()) {
                inventory.setItem(allowedSlots.get(i), dealItem);
                i++;
            }
        }

        
        return inventory;
    }
    
    public ItemStack replacePlaceholdersInLore(ItemStack item, String placeholder, Object value) {
        if(item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = item.getItemMeta().getLore();
            for (int i = 0; i < lore.size(); i++) {
                String line = lore.get(i);
                if(line.contains(placeholder)) {
                    lore.set(i, line.replaceAll(placeholder, value.toString()));
                }
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ColorsUtil.colorizeText(message));
    }
    
    /**
     * Should always pass new instances of lists here.
     * @param playerItems Items that player've provided to make a deal
     * @param sampleItems Items that playerItems should match
     * @return Boolean whether playerItems match sampleItems
     */
    public boolean listContainsNeededItems(List<ItemStack> playerItems, List<ItemStack> sampleItems) {
        if(playerItems.size() == sampleItems.size()) {
            Iterator<ItemStack> it = playerItems.iterator();
            while(it.hasNext()) {
                ItemStack givenItem = it.next();
                Iterator<ItemStack> it2 = sampleItems.iterator();
                while(it2.hasNext()) {
                    if(itemEquals(givenItem, it2.next())) {
                        it.remove();
                        it2.remove();
                        break;
                    }
                }
            }
            
            return playerItems.size() == 0;
        }
        
        return false;
    }
    
    public boolean playerFulfillsContract(MarketDeal deal, Player player, List<ItemStack> playerItems, List<ItemStack> sampleItems) {
        if(!sampleItems.isEmpty()) {
            if(this.playerListsContainsSample(playerItems, sampleItems) == 0) {
                return false;
            }
        }
        
        if(deal.getDealContract().getCurrencyCost() > 0 && MarketPoint.getEconomy() != null) {
            if(!MarketPoint.getEconomy().has(player, deal.getDealContract().getCurrencyCost())) {
                return false;
            }
        }
        
        if(!deal.getDealContract().getCustomCurrencies().isEmpty()) {
            boolean notEnoughCustomCurrency = deal.getDealContract().getCustomCurrencies().values()
                .stream()
                .filter(currency -> currencyHandlers.containsKey(currency.getType()))
                .filter(currency -> currency.getAmount() > 0)
                .anyMatch(currency -> !currencyHandlers.get(currency.getType()).playerHasAmount(player, currency.getAmount()));
            
            if(notEnoughCustomCurrency) {
                return false;
            }
        }
        
        return true;
    }
    
    private int playerListsContainsSample(List<ItemStack> playerItems, List<ItemStack> sampleItems) {
        Map<Integer, HashedItem> hashPlayerItems = this.hashifyList(playerItems);
        Map<Integer, HashedItem> hashDealItems = this.hashifyList(sampleItems);
        int canBuyAmount = 0;
        
        for(Entry<Integer, HashedItem> entry : hashDealItems.entrySet()) {
            if(!hashPlayerItems.containsKey(entry.getKey())) {
                return 0;
            }
            HashedItem playerHashedItem = hashPlayerItems.get(entry.getKey());
            int playerCanBuyAmount = (int) Math.floor(playerHashedItem.getAmount() / entry.getValue().getAmount());
            if(canBuyAmount == 0 && playerCanBuyAmount > 0) {
                canBuyAmount = playerCanBuyAmount;
            } else if(playerCanBuyAmount < canBuyAmount) {
                canBuyAmount = playerCanBuyAmount;
            }
        }
        
        return canBuyAmount;
    }
    
    public Map<Integer, HashedItem> hashifyList(List<ItemStack> itemList) {
        Map<Integer, HashedItem> map = new HashMap<>();
        
        itemList.forEach(item -> { 
            int itemHash = this.getItemHash(item);
            if(itemHash != -1) {
                HashedItem hashedItem = map.getOrDefault(itemHash, new HashedItem(item.clone(), 0));
                hashedItem.setAmount(hashedItem.getAmount() + item.getAmount());
                map.put(itemHash, hashedItem);
            }
        });
        
        return map;
    }
    
    public int getItemHash(ItemStack item) {
        if(item == null || item.getType() == Material.AIR) {
            return -1;
        }
        
        StringBuilder sb = new StringBuilder(item.getType().name());
        sb.append("durability:" + item.getDurability());
        if(item.hasItemMeta()) {
            ItemMeta itemMeta = item.getItemMeta();
            sb.append("isUnbreakable:" + itemMeta.isUnbreakable());
            sb.append("displayName:" + itemMeta.getDisplayName());
            if(itemMeta.getLore() != null) {
                sb.append("lore:" + itemMeta.getLore().toString());
            }
        }
        
        return sb.toString().hashCode();
    }
    
    public boolean itemEquals(ItemStack playerItem, ItemStack sample) {
        if(playerItem == null || sample == null || playerItem.getType() == Material.AIR || sample.getType() == Material.AIR) {
            return false;
        }
        
        if(playerItem.getType() == sample.getType()) {
            if(sample.getItemMeta().isUnbreakable()) {
                if(!playerItem.getItemMeta().isUnbreakable()) {
                    return false;
                }
                
                if(sample.getDurability() != playerItem.getDurability()) {
                    return false;
                }
            }
            
            if(sample.hasItemMeta()) {
                if(!playerItem.hasItemMeta()) {
                    return false;
                }
                
                if(sample.getItemMeta().hasDisplayName()) {
                    if(!playerItem.getItemMeta().hasDisplayName()) {
                        return false;
                    } else if(!playerItem.getItemMeta().getDisplayName().equals(sample.getItemMeta().getDisplayName())){
                        return false;
                    }
                }
                
                if(sample.getItemMeta().hasLore()) {
                    if(!playerItem.getItemMeta().hasLore()) {
                        return false;
                    } else if(!playerItem.getItemMeta().getLore().equals(sample.getItemMeta().getLore())){
                        return false;
                    }
                }
            }
            
            return true;
        }
        
        return false;
    }

    public static HashSet<Integer> getAllowedDealSlots() {
        return ALLOWED_DEAL_SLOTS;
    }
    
    private static void initalizeDealInventory() {
        Inventory inventory = Bukkit.createInventory(null,45, MarketDealEditorClickListener.MARKET_DEAL_EDITOR_INVENTORY_NAME);
        DEALER_DEAL_SLOTS.forEach(intFiller -> {
            inventory.setItem(intFiller, ButtonProvider.SECONDARY_THEME_FILLER);
        });
        
        //lower row
        for (int i = 44; i >= 36; i--) {
            inventory.setItem(i, ButtonProvider.MAIN_THEME_FILLER);
        }
        //upper row
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, ButtonProvider.MAIN_THEME_FILLER);
        }
        //middle row
        for (int i = 12; i < 15; i++) {
            inventory.setItem(i, ButtonProvider.MAIN_THEME_FILLER);
        }
        
        //two vertical points
        inventory.setItem(31, ButtonProvider.MAIN_THEME_FILLER);
        inventory.setItem(22, ButtonProvider.MAIN_THEME_FILLER);
        //other overrides
        inventory.setItem(37, ButtonProvider.BACK_TO_MARKET_BUTTON);
        
        DEFAULT_DEAL_INVENTORY = inventory;
    }
}
