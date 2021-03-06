package cz.dubcat.marketpoint.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import cz.dubcat.marketpoint.MarketPoint;
import cz.dubcat.marketpoint.api.MarketPointAPI;
import cz.dubcat.marketpoint.events.chat.ChatAction;
import cz.dubcat.marketpoint.events.chat.ChatListener;
import cz.dubcat.marketpoint.market.Market;
import cz.dubcat.marketpoint.market.MarketDeal;
import cz.dubcat.marketpoint.market.MarketPage;
import cz.dubcat.marketpoint.market.data.CurrentGui;
import cz.dubcat.marketpoint.market.data.MarketInventoryHolder;
import cz.dubcat.marketpoint.market.data.PlayerData;

public class MarketEditorClickListener implements Listener {
    
    private static final int MARKET_DEAL_EDIT = 45;
    private static final int NEXT_PAGE_ID = 52;
    private static final int PREVIOUS_PAGE_ID = 46;
    private static final int RENAME_ID = 48;
    private static final int PERMISISONS_ID = 47;
    private static final int CITIZENTS_ID = 50;
    
    @EventHandler(ignoreCancelled = true)
    public void onMarketClickEvent(InventoryClickEvent e) {
        if(e.getWhoClicked() instanceof Player) {
            Player player = (Player) e.getWhoClicked();
            
            if(e.getInventory().getHolder() instanceof MarketInventoryHolder) {
                MarketInventoryHolder holder = (MarketInventoryHolder) e.getInventory().getHolder();

                if(holder.getCurrentGui() != CurrentGui.MAIN_EDITOR) {
                    return;
                }
                
                PlayerData data = MarketPointAPI.PLAYER_MARKET_DATA.get(player.getUniqueId());
                if(data != null && data.isEditor()) {
                    Market market = MarketPointAPI.LOADED_MARKETS.get(data.getMarketId());
                    int page = data.getPage();
                    int slot = e.getRawSlot();
                    InventoryAction action = e.getAction();
                    
                    if(slot < MARKET_DEAL_EDIT) {
                        MarketPage marketPage = market.getMarketPage(page);
                        if(e.getCurrentItem() != null && action == InventoryAction.PICKUP_ALL) {
                            e.setCancelled(true);
                            data.setCurrentDealId(slot);
                            Inventory dealInventory = MarketPointAPI.inst().getDealEditorGui(market, slot, page);
                            if(dealInventory == null) {
                                e.getInventory().setItem(slot, null);
                                return;
                            }
                            player.openInventory(dealInventory);
                        } else if(e.getCursor() != null && action == InventoryAction.PLACE_ALL) {
                            MarketDeal newDeal = new MarketDeal(marketPage, String.valueOf(slot));
                            newDeal.setOffer(e.getCursor().clone());
                            marketPage.getMarketDeals()[slot] = newDeal;
                            player.getInventory().addItem(e.getCursor().clone());
                            e.getCursor().setAmount(0);
                            Inventory dealInventory = MarketPointAPI.inst().getDealEditorGui(market, slot, page);
                            player.openInventory(dealInventory);
                            data.setCurrentDealId(slot);
                            e.setCancelled(true);
                            MarketPointAPI.inst().sendMessage(player, "New deal has been added.");
                        }
                    } else if(slot == 49) {
                        e.setCancelled(true);
                        if(market.getMarketPages().size() > 1) {
                            //remove empty pages on close
                            boolean lastPageIsEmpty = true;
                            MarketPage lastPage = market.getMarketPage(market.getMarketPages().size() - 1);
                            for (int i = 0; i < lastPage.getMarketDeals().length; i++) {
                                if(lastPage.getMarketDeals()[i] != null) {
                                    lastPageIsEmpty = false;
                                    break;
                                }
                            }
                            
                            if(lastPageIsEmpty) {
                                if(data.getPage() == market.getMarketPages().size() - 1) {
                                    data.setPage(data.getPage() - 1);
                                }
                                market.getMarketPages().remove(market.getMarketPages().size() - 1);
                            }
                        }
                        
                        data.setEditor(false);
                        Inventory marketInvenotry = MarketPointAPI.inst().getMarketGui(market, data.getPage(), player, false);
                        player.openInventory(marketInvenotry);
                    } else if(slot == NEXT_PAGE_ID) {
                        if(page + 1 > MarketPointAPI.MAX_PAGES_PER_MARKET) {
                            MarketPointAPI.inst().sendMessage(player, "&4You have reached maximum pages allowed per market (" + (MarketPointAPI.MAX_PAGES_PER_MARKET + 1) +") HOW LOL?");
                        } else {
                            Inventory nextPageMarketInventory = MarketPointAPI.inst().getMarketGui(market, page + 1, player, true);
                            if(nextPageMarketInventory != null) {
                                data.setPage(page + 1);
                                player.openInventory(nextPageMarketInventory);
                            } else {
                                MarketPointAPI.inst().sendMessage(player, "&cYou have to put at least one item into the page.");
                            }
                        }
                        e.setCancelled(true);
                    } else if (slot == PREVIOUS_PAGE_ID) {
                        Inventory previousPageMarketInventory = MarketPointAPI.inst().getMarketGui(market, page - 1, player, true);
                        data.setPage(page - 1);
                        if(data.getPage() < 0) {
                            data.setPage(0);
                        }
                        player.openInventory(previousPageMarketInventory);
                        e.setCancelled(true);
                    } else if (slot == RENAME_ID) {
                        e.setCancelled(true);
                        ChatListener.CHAT_MAP.put(player.getUniqueId(), ChatAction.PAGE_RENAME);
                        player.closeInventory();
                        MarketPointAPI.inst().sendMessage(player, "&aPlease provide a name for the view:");
                    } else if(slot == PERMISISONS_ID) {
                        if(action == InventoryAction.PICKUP_HALF) {
                            market.setPermissionToOpen(null);
                            MarketPointAPI.inst().sendMessage(player, "&aYou've cleared permission to open for market "+market.getMarketId());
                            player.closeInventory();
                            Inventory marketEditorMain = MarketPointAPI.inst().getMarketGui(MarketPointAPI.LOADED_MARKETS.get(data.getMarketId()), data.getPage(), player, true);
                            Bukkit.getScheduler().runTask(MarketPoint.getPlugin(), () -> {
                                player.openInventory(marketEditorMain);
                            });
                        } else {
                            e.setCancelled(true);
                            ChatListener.CHAT_MAP.put(player.getUniqueId(), ChatAction.PERMISSION_CHANGE);
                            player.closeInventory();
                            MarketPointAPI.inst().sendMessage(player, "&aPlease write a permission that will be required to open market "+market.getMarketId()+":");
                        }
                    } else if(slot == CITIZENTS_ID && MarketPoint.isCitizentsLoaded()) {
                        e.setCancelled(true);
                        ChatListener.CHAT_MAP.put(player.getUniqueId(), ChatAction.CITIZENTS_ID);
                        player.closeInventory();
                        MarketPointAPI.inst().sendMessage(player, "&aPlease write NPC's id to add or to remove NPC's id from the list write &7del:ncpId &f):");
                    } else if(slot < e.getInventory().getSize()) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
}
