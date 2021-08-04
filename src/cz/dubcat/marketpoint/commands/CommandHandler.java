package cz.dubcat.marketpoint.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

import cz.dubcat.marketpoint.lang.Translations;

public class CommandHandler implements TabExecutor {

  private HashMap<String, CommandInterface> commands = new HashMap<>();
  private String mainCommand = "marketpoint";
  
  public CommandHandler(String mainCommand) {
      this.mainCommand = mainCommand;
  }
  
  public void registerMenu(CommandInterface cmd) {
      commands.put(mainCommand, cmd);
  }
  
  public void register(String name, CommandInterface cmd) {
      commands.put(name, cmd);
  }

  public boolean exists(String name) {
      return commands.containsKey(name);
  }

  public CommandInterface getExecutor(String name) {
      return commands.get(name);
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
      if(args.length == 0) {
          getExecutor(mainCommand).onCommand(sender, cmd, commandLabel, args);
          return true;
      }

      if(args.length > 0) {
          if(exists(args[0])){
            getExecutor(args[0]).onCommand(sender, cmd, commandLabel, args);
            
            return true;
          } else {
              Translations.translateAndSend(sender, "lang.commandDoesNotExist");

              return true;
          }
      }
      return false;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      return args.length == 1
              ? StringUtil.copyPartialMatches(args[0], commands.keySet(), new ArrayList<>())
              : null;
  }

}