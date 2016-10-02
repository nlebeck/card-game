package niellebeck.cardgameserver.messaging;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;

public class TcpMessager {
    
    private static ServerSocketChannel serverSocketChannel = null;
    private static SelectionKey serverSocketSelectionKey;
    private static Selector selector = null;
    
    public static void init(int listeningPort) throws IOException {
        selector = Selector.open();
        
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(listeningPort));
        serverSocketChannel.configureBlocking(false);
        
        serverSocketSelectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }
    
    public static void startMessageLoop(MessageCallback callback) throws IOException {
        while(true) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            for (SelectionKey key : selectedKeys) {
                if (key.equals(serverSocketSelectionKey)) {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    
                    String[] result = receiveMessage(socketChannel.socket());
                    String address = result[0];
                    String message = result[1];
                    
                    callback.callback(address, message);
                }
            }
        }
    }
    
    private static String[] receiveMessage(Socket socket) throws IOException {
        String address = socket.getInetAddress().getHostAddress();
        
        byte[] messageLengthBytes = new byte[4];
        socket.getInputStream().read(messageLengthBytes);
        int messageLength = (messageLengthBytes[0] << 24) | (messageLengthBytes[1] << 16) | (messageLengthBytes[2] << 8) | messageLengthBytes[3];
        
        byte[] messageBytes = new byte[messageLength];
        socket.getInputStream().read(messageBytes);
        char[] messageChars = new char[messageLength];
        for (int i = 0; i < messageBytes.length; i++) {
            messageChars[i] = (char)messageBytes[i];
        }
        String message = String.valueOf(messageChars, 0, messageChars.length);
        
        socket.close();
        
        String[] result = new String[2];
        result[0] = address;
        result[1] = message;
        
        return result;
    }
    
    public static void sendMessage(String message, String hostname, int port) throws IOException {
        Socket socket = new Socket(hostname, port);
        
        byte[] bytes = new byte[4];
        bytes[0] = (byte)((message.length() >> 24) & 0xFF);
        bytes[1] = (byte)((message.length() >> 16) & 0xFF);
        bytes[2] = (byte)((message.length() >> 8) & 0xFF);
        bytes[3] = (byte)(message.length() & 0xFF);
        
        socket.getOutputStream().write(bytes);
        
        byte[] messageBytes = new byte[message.length()];
        char[] messageChars = message.toCharArray();
        for (int i = 0; i < message.length(); i++)
        {
            messageBytes[i] = (byte)messageChars[i];
        }
        
        socket.getOutputStream().write(messageBytes);
        
        socket.close();
    }
}
