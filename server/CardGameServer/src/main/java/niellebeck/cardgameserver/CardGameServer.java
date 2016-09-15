package niellebeck.cardgameserver;

import java.io.IOException;

import niellebeck.cardgameserver.messaging.TcpMessager;

/**
 * The server for the card game
 *
 */
public class CardGameServer
{
    private static final int SERVER_PORT = 8080;
    
    public static void main( String[] args )
    {        
        TcpMessager tcpMessager = null;
        try {
            tcpMessager = TcpMessager.getTcpMessager(SERVER_PORT);
        }
        catch (IOException e) {
            System.err.println("Error creating TcpMessager: " + e);
            System.exit(1);
        }
        
        while (true) {
            String message = null;
            try {
                message = tcpMessager.receiveMessage();
            }
            catch (IOException e) {
                System.err.println("Error receiving message: " + e);
            }
            System.out.println(message);
        }
    }
}
