package niellebeck.cardgameserver.messaging;

import java.net.SocketAddress;

import niellebeck.cardgameserver.messages.JsonMessage;

public interface JsonMessageCallback {
    public void callback(SocketAddress address, JsonMessage message);
}
