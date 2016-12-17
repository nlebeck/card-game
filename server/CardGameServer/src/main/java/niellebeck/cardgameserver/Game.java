package niellebeck.cardgameserver;

import java.util.ArrayList;
import java.util.List;

public class Game {
    String gameName;
    private List<Player> players;
    private boolean started;
    
    private int currentPlayerNum;
    
    public Game(String gameName) {
        this.gameName = gameName;
        this.players = new ArrayList<Player>();
        this.started = false;
    }
    
    public void joinGame(String playerName) {
        Player player = new Player(playerName);
        players.add(player);
    }
    
    public void start() {
        started = true;
    }
    
    public void playCard(String playerName, String cardName) {
        //TODO: add some game logic
        //TODO: add more parameters to this method if cards can have targets
    }
    
    public int getNumPlayers() {
        return players.size();
    }
    
    public boolean isStarted() {
        return started;
    }
    
    public String getGameName() {
        return gameName;
    }
}
