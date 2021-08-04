package cz.dubcat.marketpoint.commands.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import cz.dubcat.marketpoint.api.MarketPointAPI;
import cz.dubcat.marketpoint.commands.CommandInterface;
import cz.dubcat.marketpoint.commands.CommandUtils;

public class MarketListCommand implements CommandInterface {
    @Override
    public void onCommand(CommandSender sender, Command paramCommand, String paramString, String[] paramArrayOfString) {
        if(CommandUtils.isAdmin(sender)) {
            sender.sendMessage("Available market ids:");
            MarketPointAPI.LOADED_MARKETS.forEach((key, market) -> {
                sender.sendMessage("- " + key);
            });
        }
    }

}