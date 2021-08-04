package cz.dubcat.marketpoint.commands.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cz.dubcat.marketpoint.api.MarketPointAPI;
import cz.dubcat.marketpoint.commands.CommandInterface;
import cz.dubcat.marketpoint.lang.Translations;
import cz.dubcat.marketpoint.market.Market;

public class OpenMarketCommand implements CommandInterface {

    @Override
    public void onCommand(CommandSender sender, Command paramCommand, String paramString, String[] paramArrayOfString) {
        Player player = (Player) sender;

        if(paramArrayOfString.length == 2) {
            String marketName = paramArrayOfString[1];
            Market market = MarketPointAPI.LOADED_MARKETS.get(marketName);

            if(market != null) {
                new MarketPointAPI().openMarket(marketName, player);
            } else {
                String translation = Translations.translateWithPrefix("lang.marketDoesNotExist");
                sender.sendMessage(translation.replaceAll("%market_id%", marketName));
            }
        } else {
            sender.sendMessage("Please provide market name.");
        }
    }

}
