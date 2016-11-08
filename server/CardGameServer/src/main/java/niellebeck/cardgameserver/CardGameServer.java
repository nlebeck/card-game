package niellebeck.cardgameserver;

import java.io.IOException;
import java.net.SocketAddress;

import niellebeck.cardgameserver.messaging.MessageCallback;
import niellebeck.cardgameserver.messaging.TcpMessenger;

/**
 * The server for the card game
 *
 */
public class CardGameServer
{
    private static final int SERVER_PORT = 8080;
    
    public static void main( String[] args )
    {        
        try {
            TcpMessenger.init(SERVER_PORT);
        }
        catch (IOException e) {
            System.err.println("Error creating TcpMessenger: " + e);
            System.exit(1);
        }
        
        TcpMessenger.startMessageLoop(new MessageCallback() {
            public void callback(SocketAddress address, String message) {
                String addressStr = address.toString().substring(1).split(":")[0];
                System.out.println("Message received from " + addressStr + ": " + message);
                TcpMessenger.sendMessage(address, "Message received: " + message);
            }
        });
    }
}
