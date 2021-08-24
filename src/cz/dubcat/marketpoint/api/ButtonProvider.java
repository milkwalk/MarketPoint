package cz.dubcat.marketpoint.api;

import java.util.Optional;

import org.bukkit.inventory.ItemStack;

import com.cryptomorin.xseries.XMaterial;

import cz.dubcat.marketpoint.MarketPoint;
import cz.dubcat.marketpoint.lang.Translations;
import cz.dubcat.marketpoint.utils.ItemBuilder;

public class ButtonProvider {
    public static ItemStack PREVIOUS_PAGE_BUTTON;
    public static ItemStack NEXT_PAGE_BUTTON;
    public static ItemStack MAIN_THEME_FILLER;
    public static ItemStack SECONDARY_THEME_FILLER;
    public static ItemStack BACK_TO_MARKET_BUTTON;
    public static ItemStack SUCCESS_THEME;
    public static ItemStack CURRENCY_INFO;

    // admin editor buttons
    public static ItemStack ENTER_EDITOR_MODE_BUTTON;
    public static ItemStack LEAVE_EDITOR_MODE_BUTTON;
    public static ItemStack DELETE_DEAL_BUTTON;
    public static ItemStack RENAME_INVENTORY_BUTTON;
    public static ItemStack CITIZENTS_BUTTON;
    public static ItemStack CURRENCY_EDIT_BUTTON;
    public static ItemStack CUSTOM_CURRENCY_EDIT_BUTTON;
    public static ItemStack PERMISSIONS_BUTTON;

    public static void initializeButtons() {
        CURRENCY_INFO = new ItemBuilder(getItemStackForMaterial("EMERALD"))
                .name(Translations.translate("lang.market.currencyInfo")).lore("placeholder").make();
        MAIN_THEME_FILLER = new ItemBuilder(XMaterial.PURPLE_STAINED_GLASS_PANE.parseItem()).name(" ").make();
        SECONDARY_THEME_FILLER = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE.parseItem()).name(" ").make();
        PREVIOUS_PAGE_BUTTON = new ItemBuilder(XMaterial.PAPER.parseItem())
                .name(Translations.translate("lang.market.previousPage")).make();
        NEXT_PAGE_BUTTON = new ItemBuilder(XMaterial.PAPER.parseItem())
                .name(Translations.translate("lang.market.nextPage")).make();
        ENTER_EDITOR_MODE_BUTTON = new ItemBuilder(XMaterial.WRITABLE_BOOK.parseItem()).name("&aEnter editor mode")
                .make();
        LEAVE_EDITOR_MODE_BUTTON = new ItemBuilder(XMaterial.WRITTEN_BOOK.parseItem()).name("&cLeave editor mode")
                .make();
        BACK_TO_MARKET_BUTTON = new ItemBuilder(XMaterial.ARROW.parseItem())
                .name(Translations.translate("lang.market.backToMarket")).make();
        BACK_TO_MARKET_BUTTON = new ItemBuilder(XMaterial.ARROW.parseItem())
                .name(Translations.translate("lang.market.backToMarket")).make();
        DELETE_DEAL_BUTTON = new ItemBuilder(XMaterial.BARRIER.parseItem()).name("&cCancel deal")
                .lore("&cRemoves deal completely!").make();
        SUCCESS_THEME = new ItemBuilder(XMaterial.LIME_STAINED_GLASS_PANE.parseItem()).name(" ").make();
        CURRENCY_EDIT_BUTTON = new ItemBuilder(XMaterial.EMERALD.parseItem()).name("&aEdit currency")
                .lore("Current value: &a%current_currency_value%").lore("&7Set to 0 to ignore this.").colorizeLore()
                .make();
        RENAME_INVENTORY_BUTTON = new ItemBuilder(XMaterial.NAME_TAG.parseItem()).name("&7Rename market view")
                .lore("&fSets current view name").colorizeLore().make();
        CUSTOM_CURRENCY_EDIT_BUTTON = new ItemBuilder(XMaterial.DIAMOND.parseItem()).name("&7Edit custom currencies")
                .lore("&fCurrently none are set").colorizeLore().make();
        PERMISSIONS_BUTTON = new ItemBuilder(getItemStackForMaterial("REDSTONE_TORCH")).name("&7Edit permission").lore("&fRequired permission to open the market")
                .lore("&fTo remove permission right click the icon.").lore("").lore("&6Current value: &a%value%").colorizeLore().make();
    }

    public static void loadCitizetndsButtons() {
        CITIZENTS_BUTTON = new ItemBuilder(XMaterial.PLAYER_HEAD.parseItem()).name("&7Assign NPC")
                .lore("&fAssigns NPC to the market").lore("&7To remove NPC write &adel:npcId").lore("").lore("&6Current value: &a%value%").colorizeLore().make();
    }

    private static ItemStack getItemStackForMaterial(String materialName) {
        Optional<XMaterial> currencyInfoMat = XMaterial.matchXMaterial(materialName);

        if (currencyInfoMat.isPresent()) {
            return currencyInfoMat.get().parseItem();
        }

        MarketPoint.getLog().warning("Unknown material '" + materialName + "' name, please check you'r config, replacing it with STONE.");

        return XMaterial.STONE.parseItem();
    }
}
