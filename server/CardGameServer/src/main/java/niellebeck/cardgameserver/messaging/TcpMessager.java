package niellebeck.cardgameserver.messaging;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;

public class TcpMessager {
    private static ServerSocketChannel serverSocketChannel = null;
    private static SelectionKey serverSocketSelectionKey;
    private static List<SelectionKey> clientSocketSelectionKeys;
    private static Selector selector = null;
    
    private static Map<SocketAddress, ClientInfo> clientInfoMap;
    
    public static void init(int listeningPort) throws IOException {
        clientSocketSelectionKeys = new ArrayList<SelectionKey>();
        clientInfoMap = new HashMap<SocketAddress, ClientInfo>();
        
        selector = Selector.open();
        
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(listeningPort));
        serverSocketChannel.configureBlocking(false);
        
        serverSocketSelectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }
    
    public static void startMessageLoop(MessageCallback callback) throws IOException {
        while(true) {
            selector.select();
            for (SelectionKey key : selector.selectedKeys()) {
                if (key.equals(serverSocketSelectionKey)) {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    SelectionKey clientKey = socketChannel.register(selector, SelectionKey.OP_READ);
                    clientSocketSelectionKeys.add(clientKey);
                }
                else if (clientSocketSelectionKeys.contains(key)) {
                    receiveMessage(((SocketChannel)key.channel()), callback);
                }
            }
            selector.selectedKeys().clear();
        }

    }
    
    private static void receiveMessage(SocketChannel socketChannel, MessageCallback callback) throws IOException {
        SocketAddress address = socketChannel.getRemoteAddress();
        
        if (!clientInfoMap.containsKey(address)) {
            clientInfoMap.put(address, new ClientInfo());
        }
        
        ClientInfo clientInfo = clientInfoMap.get(address);
        
        if (clientInfo.state == ClientState.IDLE) {
            clientInfo.state = ClientState.SENDING_LENGTH;
        }
        
        if (clientInfo.state == ClientState.SENDING_LENGTH) {
            socketChannel.read(clientInfo.messageLengthBuffer);
            if (clientInfo.messageLengthBuffer.position() == 4) {
                clientInfo.messageLengthBuffer.flip();
                byte[] messageLengthBytes = new byte[4];
                clientInfo.messageLengthBuffer.get(messageLengthBytes);
                
                int messageLength = (messageLengthBytes[0] << 24) | (messageLengthBytes[1] << 16) | (messageLengthBytes[2] << 8) | messageLengthBytes[3];
                if (messageLength > ClientInfo.MAX_MESSAGE_LENGTH) {
                    throw new IOException("Oversize message (" + messageLength + " bytes)");
                }
                
                clientInfo.messageLength = messageLength;
                clientInfo.state = ClientState.SENDING_MESSAGE;
            }
        }
        
        if (clientInfo.state == ClientState.SENDING_MESSAGE) {
            socketChannel.read(clientInfo.messageBuffer);
            if (clientInfo.messageBuffer.position() == clientInfo.messageLength) {
                clientInfo.messageBuffer.flip();
                byte[] messageBytes = new byte[clientInfo.messageLength];
                clientInfo.messageBuffer.get(messageBytes);
                
                char[] messageChars = new char[messageBytes.length];
                for (int i = 0; i < messageBytes.length; i++) {
                    messageChars[i] = (char)messageBytes[i];
                }
                String message = String.valueOf(messageChars, 0, messageChars.length);
                
                String addressStr = address.toString().substring(1).split(":")[0];
                callback.callback(addressStr, message);
                
                socketChannel.close();
                
                clientInfo.state = ClientState.IDLE;
                clientInfo.messageLengthBuffer.clear();
                clientInfo.messageBuffer.clear();
                clientInfo.messageLength = -1;
            }
        }
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
