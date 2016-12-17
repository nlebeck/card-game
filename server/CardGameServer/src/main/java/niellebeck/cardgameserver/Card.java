package niellebeck.cardgameserver;

public class Card {
    private String cardName;
    
    public Card(String cardName) {
        this.cardName = cardName;
    }
    
    @Override
    public boolean equals(Object otherObj) {
        Card otherCard = (Card)otherObj;
        return this.cardName.equals(otherCard.cardName);
    }
    
    @Override
    public int hashCode() {
        return this.cardName.hashCode();
    }
}
