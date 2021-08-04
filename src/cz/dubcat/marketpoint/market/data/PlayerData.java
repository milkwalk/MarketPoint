package cz.dubcat.marketpoint.market.data;

import lombok.Data;

@Data
public class PlayerData {
    private String marketId;
    private int page = 0;
    private boolean editor = false;
    private int currentDealId = 0;
    
    public PlayerData(String marketId) {
        this.marketId = marketId;
    }
}
