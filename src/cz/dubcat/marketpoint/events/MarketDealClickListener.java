package cz.dubcat.marketpoint.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import cz.dubcat.marketpoint.MarketPoint;
import cz.dubcat.marketpoint.api.ButtonProvider;
import cz.dubcat.marketpoint.api.MarketPointAPI;
import cz.dubcat.marketpoint.lang.Translations;
import cz.dubcat.marketpoint.market.HashedItem;
import cz.dubcat.marketpoint.market.Market;
import cz.dubcat.marketpoint.market.MarketDeal;
import cz.dubcat.marketpoint.market.data.CurrentGui;
import cz.dubcat.marketpoint.market.data.MarketInventoryHolder;
import cz.dubcat.marketpoint.market.data.PlayerData;

public class MarketDealClickListener implements Listener {
    private Set<InventoryAction> allowedActions = new HashSet<>(Arrays.asList(InventoryAction.PICKUP_ALL, InventoryAction.PLACE_ALL, 
            InventoryAction.PLACE_ONE, InventoryAction.PLACE_SOME, InventoryAction.PICKUP_ONE, InventoryAction.PICKUP_SOME, InventoryAction.PICKUP_HALF, InventoryAction.MOVE_TO_OTHER_INVENTORY));
    private final int BUY_SLOT = 4;
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMarketDealEditorClick(InventoryClickEvent e) {
        if(!(e.getWhoClicked() instanceof Player)) {
            return;
        }
        
        int slot = e.getRawSlot();
        Player player = (Player) e.getWhoClicked();
        if(e.getInventory().getHolder() instanceof MarketInventoryHolder) {
            MarketInventoryHolder holder = (MarketInventoryHolder) e.getInventory().getHolder();

            if(holder.getCurrentGui() != CurrentGui.DEAL) {
                return;
            }
            
            try {
                PlayerData playerData = MarketPointAPI.PLAYER_MARKET_DATA.get(player.getUniqueId());
                Market market = MarketPointAPI.LOADED_MARKETS.get(playerData.getMarketId());
                MarketDeal deal = market.getMarketPage(playerData.getPage()).getMarketDeals()[playerData.getCurrentDealId()];
                
                if(deal == null) {
                    playerData.setPage(0);
                    Inventory marketMain = MarketPointAPI.inst().getMarketGui(market, 0, player, false);
                    player.openInventory(marketMain);

                    return;
                }
                
                int page = playerData.getPage();
                InventoryAction action = e.getAction();
                
                if(!allowedActions.contains(action)) {
                    e.setCancelled(true);
                    
                    return;
                } else if(action == InventoryAction.MOVE_TO_OTHER_INVENTORY && slot != BUY_SLOT) {
                    e.setCancelled(true);

                    return;
                }

                if(deal.getDealContract().getItems().size() > 0) {
                    Bukkit.getScheduler().runTaskLater(MarketPoint.getPlugin(), () -> {
                        List<ItemStack> playerItems = this.getPlayerItemList(e.getInventory());
                        
                        boolean fulfillsContract = MarketPointAPI.inst().playerFulfillsContract(deal, player, playerItems, new ArrayList<>(deal.getDealContract().getItems()));

                        if (fulfillsContract) {
                            this.greenAnimation(e.getInventory());
                        } else {
                            this.normalAnimation(e.getInventory());
                        }
                    }, 1);
                }
                
                if (slot == 37) {
                    e.setCancelled(true);
                    Inventory marketMain = MarketPointAPI.inst().getMarketGui(market, page, player, false);
                    player.openInventory(marketMain);
                } else if(slot == BUY_SLOT && (e.getCursor() == null || e.getCursor().getType().isAir())) {
                    boolean instantantBuy = action == InventoryAction.MOVE_TO_OTHER_INVENTORY;
                    boolean shouldCancelEvent = this.buyItem(player, e.getInventory(), deal, instantantBuy);

                    if(shouldCancelEvent) {
                        e.setCancelled(true);
                    } else {
                        Bukkit.getScheduler().runTaskLater(MarketPoint.getPlugin(), () -> { 
                            player.updateInventory();
                        }, 1);
                        MarketPoint.getPurchaseLogger().info("Player " + player.getName() + " has bought deal: " + deal.toString() + " from market: " + market.getMarketId());
                    }
                } else if(!MarketPointAPI.getAllowedDealSlots().contains(slot) && slot < e.getInventory().getSize()) {
                    e.setCancelled(true);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                e.setCancelled(true);
                MarketPointAPI.PLAYER_MARKET_DATA.remove(player.getUniqueId());
                player.closeInventory();
            }
        }
    }
    
    private boolean buyItem(Player player, Inventory inv, MarketDeal deal, boolean instantBuy) {
        if(deal.getDealContract().isEmpty()) {
            Translations.translateAndSend(player, "lang.itemAppearsToHaveNoPrice");

            return true;
        }
        
        List<ItemStack> playerItems = this.getPlayerItemList(inv);
        List<ItemStack> contractItems = new ArrayList<>(deal.getDealContract().getItems());
        boolean fulfills = MarketPointAPI.inst().playerFulfillsContract(deal, player, playerItems, contractItems);
        
        if(!fulfills) {
            return true;
        }
        
        Map<Integer, HashedItem> contractHashedItems = MarketPointAPI.inst().hashifyList(contractItems);
        
        for(Entry<Integer, HashedItem> entry: contractHashedItems.entrySet()) {
            int requiredToTake = entry.getValue().getAmount();
            int taken = 0;
            for(ItemStack playerItem: playerItems) {
                if(MarketPointAPI.inst().itemEquals(playerItem, entry.getValue().getItem())) {
                    int leftToTake = requiredToTake - taken;
                    
                    if(playerItem.getAmount() >= leftToTake) {
                        taken = requiredToTake;
                        playerItem.setAmount(playerItem.getAmount() - leftToTake);
                    } else {
                        taken += playerItem.getAmount();
                        playerItem.setAmount(0);
                    }
                    
                    if(taken >= requiredToTake) {
                        break;
                    }
                }
            }
            
            if(taken < requiredToTake) {
                return true;
            }
        }
        
        if(deal.getDealContract().getCurrencyCost() > 0 && MarketPoint.getEconomy() != null) {
            MarketPoint.getEconomy().withdrawPlayer(player, deal.getDealContract().getCurrencyCost());
        }
        
        deal.getDealContract().getFilteredCustomCurrencies().forEach(c -> { 
            MarketPointAPI.getCurrencyHandlers().get(c.getType()).subtractAmountFromPlayer(player, c.getAmount());
        });
        
        
        if(instantBuy) {
            player.getInventory().addItem(deal.getOffer().clone());
        }
        
        Bukkit.getScheduler().runTaskLater(MarketPoint.getPlugin(), () -> {
            if(inv != null) {
                inv.setItem(4, deal.getOffer().clone());
                boolean fulfillsAgain = MarketPointAPI.inst().playerFulfillsContract(deal, player, playerItems, contractItems);
                
                if(!fulfillsAgain) {
                    this.normalAnimation(inv);
                }
            }
        }, 1);
        
        if(!instantBuy && Math.random() < 0.5) {
            Translations.translateAndSend(player, "lang.shiftClickTip");
        }
        
        return instantBuy;
    }
    
    private List<ItemStack> getPlayerItemList(Inventory inv, ItemStack ...items) {
        List<ItemStack> itemsList = new ArrayList<>();
        for (Integer alloweSlot : MarketPointAPI.getAllowedDealSlots()) {
            ItemStack allowedSlotItem = inv.getItem(alloweSlot);
            if(allowedSlotItem != null) {
                itemsList.add(allowedSlotItem);
            }
        }
        itemsList.addAll(Arrays.asList(items));
        
        return itemsList;
    }
    
    private void greenAnimation(Inventory inv) {
        for (int i = 0; i < inv.getContents().length; i++) {
            ItemStack item = inv.getContents()[i];
            if(item != null && item.equals(ButtonProvider.MAIN_THEME_FILLER)) {
                inv.setItem(i,  ButtonProvider.SUCCESS_THEME.clone());
            }
        }
    }
    
    private void normalAnimation(Inventory inv) {
        for (int i = 0; i < inv.getContents().length; i++) {
            ItemStack item = inv.getContents()[i];
            if(item != null && item.equals(ButtonProvider.SUCCESS_THEME)) {
                inv.setItem(i,  ButtonProvider.MAIN_THEME_FILLER.clone());
            }
        }
    }
}
