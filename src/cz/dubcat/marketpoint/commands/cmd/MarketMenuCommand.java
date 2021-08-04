package cz.dubcat.marketpoint.commands.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import cz.dubcat.marketpoint.commands.CommandInterface;
import cz.dubcat.marketpoint.commands.CommandUtils;
import cz.dubcat.marketpoint.lang.Translations;

public class MarketMenuCommand implements CommandInterface {

    @Override
    public void onCommand(CommandSender sender, Command paramCommand, String paramString, String[] paramArrayOfString) {
        sender.sendMessage(Translations.translateWithPrefix("lang.menu.open"));
        sender.sendMessage(Translations.translateWithPrefix("lang.menu.list"));

        if(CommandUtils.isAdmin(sender)) {
            sender.sendMessage(Translations.translateWithPrefix("lang.menu.create"));
            sender.sendMessage(Translations.translateWithPrefix("lang.menu.delete"));
            sender.sendMessage(Translations.translateWithPrefix("lang.menu.reload"));
        }
    }

}
