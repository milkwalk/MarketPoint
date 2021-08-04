package cz.dubcat.marketpoint.commands.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import cz.dubcat.marketpoint.MarketPoint;
import cz.dubcat.marketpoint.api.MarketPointAPI;
import cz.dubcat.marketpoint.commands.CommandInterface;
import cz.dubcat.marketpoint.commands.CommandUtils;

public class DeleteMarketCommand implements CommandInterface {

    @Override
    public void onCommand(CommandSender sender, Command command, String commandLabel, String[] params) {
        if(CommandUtils.isAdmin(sender)) {
            if(params.length == 2) {
                String newMarketId = params[1];
                if(MarketPointAPI.LOADED_MARKETS.containsKey(newMarketId)) {
                    if(MarketPoint.getMarketDataManager().getMarketFile(newMarketId).exists()) {
                        MarketPoint.getMarketDataManager().getMarketFile(newMarketId).delete();
                    }
                    MarketPointAPI.LOADED_MARKETS.remove(newMarketId);
                    MarketPointAPI.inst().sendMessage(sender, "&aYou have successfuly deleted market with ID " + newMarketId);
                } else {
                    MarketPointAPI.inst().sendMessage(sender, "Market " + newMarketId + " doesnt exist.");
                }
            } else {
                MarketPointAPI.inst().sendMessage(sender, "Please provide market ID &7(e.g. potion_market)");
            }
        }
    }

}
