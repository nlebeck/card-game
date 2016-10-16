package niellebeck.cardgameserver.messaging;

import java.net.SocketAddress;

public interface MessageCallback {
    public void callback(SocketAddress address, String message);
}
