package cz.dubcat.marketpoint.commands.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import cz.dubcat.marketpoint.api.MarketPointAPI;
import cz.dubcat.marketpoint.commands.CommandInterface;
import cz.dubcat.marketpoint.commands.CommandUtils;
import cz.dubcat.marketpoint.market.Market;

public class CreteMarketCommand implements CommandInterface {

    @Override
    public void onCommand(CommandSender sender, Command command, String commandLabel, String[] params) {
        if(CommandUtils.isAdmin(sender)) {
            if(params.length == 2) {
                String newMarketId = params[1];

                if(!MarketPointAPI.LOADED_MARKETS.containsKey(newMarketId)) {
                    MarketPointAPI.LOADED_MARKETS.put(newMarketId, new Market(newMarketId));
                    MarketPointAPI.inst().sendMessage(sender, "&aYou have successfuly created new market with ID " + newMarketId);
                } else {
                    MarketPointAPI.inst().sendMessage(sender, "Market " + newMarketId + " already exists.");
                }
            } else {
                MarketPointAPI.inst().sendMessage(sender, "Please provide new market ID &7(e.g. potion_market)");
            }
        }
    }

}
