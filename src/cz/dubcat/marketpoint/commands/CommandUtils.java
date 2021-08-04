package cz.dubcat.marketpoint.commands;

import org.bukkit.command.CommandSender;

public class CommandUtils {
    public static boolean isAdmin(CommandSender sender) {
        return sender.isOp() || sender.hasPermission("marketpoint.admin");
    }
}
