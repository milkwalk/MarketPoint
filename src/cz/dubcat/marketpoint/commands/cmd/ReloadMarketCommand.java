package cz.dubcat.marketpoint.commands.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import cz.dubcat.marketpoint.MarketPoint;
import cz.dubcat.marketpoint.api.ButtonProvider;
import cz.dubcat.marketpoint.commands.CommandInterface;
import cz.dubcat.marketpoint.commands.CommandUtils;
import cz.dubcat.marketpoint.lang.Translations;

public class ReloadMarketCommand implements CommandInterface {

    @Override
    public void onCommand(CommandSender sender, Command command, String commandLabel, String[] params) {
        if(CommandUtils.isAdmin(sender)) {
            MarketPoint.getPlugin().reloadConfig();
            ButtonProvider.initializeButtons();
            Translations.loadTranslations();
            
            Translations.sendMessage(sender, "&aReloaded plugin.");
        }
    }

}
