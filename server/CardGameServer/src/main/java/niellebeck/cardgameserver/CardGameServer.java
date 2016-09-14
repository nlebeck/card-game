package niellebeck.cardgameserver;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import niellebeck.cardgameserver.handlers.DefaultHandler;

/**
 * The server for the card game
 *
 */
public class CardGameServer extends AbstractHandler
{
    private static final int PORT = 8080;
    
    public static void main( String[] args )
    {
    	Server server = new Server(PORT);
    	server.setHandler(new DefaultHandler());
    	
    	try {
    	    server.start();
    	    server.join();
    	}
    	catch (Exception e) {
    	    System.err.println("Error starting server: " + e.getMessage());
    	    System.exit(1);
    	}
    }
}
