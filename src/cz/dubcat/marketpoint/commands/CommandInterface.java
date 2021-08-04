package cz.dubcat.marketpoint.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface CommandInterface {
    public void onCommand(CommandSender sender, Command command, String commandLabel, String[] params);
}
