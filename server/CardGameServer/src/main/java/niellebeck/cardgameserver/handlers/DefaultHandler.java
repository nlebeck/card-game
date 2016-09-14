package niellebeck.cardgameserver.handlers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class DefaultHandler extends AbstractHandler {
    
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request,
            HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_OK);
        try {
            response.getWriter().println("Hello client!");
        } catch (IOException e) {
            System.err.println("Error handling client request: " + e.getMessage());
        }
        baseRequest.setHandled(true);
    }
}
