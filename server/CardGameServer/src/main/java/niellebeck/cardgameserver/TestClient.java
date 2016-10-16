package niellebeck.cardgameserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TestClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", 8080));
            byte[] messageLengthBytes = {0, 0, 0, 1};
            socket.getOutputStream().write(messageLengthBytes);
            byte[] message = {'a'};
            socket.getOutputStream().write(message);
            
            byte[] replyLengthBytes = new byte[4];
            socket.getInputStream().read(replyLengthBytes);
            System.out.println(replyLengthBytes[0] + replyLengthBytes[1] + replyLengthBytes[2] + replyLengthBytes[3]);
        }
        catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
