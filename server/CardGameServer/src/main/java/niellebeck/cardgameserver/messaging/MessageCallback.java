package niellebeck.cardgameserver.messaging;

public interface MessageCallback {
    public void callback(String address, String message);
}
