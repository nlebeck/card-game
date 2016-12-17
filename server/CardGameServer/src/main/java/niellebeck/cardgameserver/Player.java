package niellebeck.cardgameserver;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String playerName;
    private List<Card> cards;
    
    public Player(String playerName) {
        this.playerName = playerName;
        this.cards = new ArrayList<Card>();
    }
    
    public boolean hasCard(String cardName) {
        Card card = new Card(cardName);
        return cards.contains(card);
    }
}
