package niellebeck.cardgameserver.messaging;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.Selector;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class TcpMessager {
    private static ServerSocketChannel serverSocketChannel = null;
    private static SelectionKey serverSocketSelectionKey;
    private static Selector selector = null;
    
    private static ClientInfoContainer clientInfoContainer;
    
    public static void init(int listeningPort) throws IOException {
        clientInfoContainer = new ClientInfoContainer();
        
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
                    
                    ClientInfo clientInfo = new ClientInfo(clientKey);
                    if (!clientInfoContainer.containsSocketAddress(clientInfo.getAddress())) {
                        clientInfoContainer.add(clientInfo);
                    }
                }
                else if (clientInfoContainer.containsSelectionKey(key)) {
                    readClientData(clientInfoContainer.get(key), callback);
                }
            }
            selector.selectedKeys().clear();
        }

    }
    
    private static void readClientData(ClientInfo clientInfo, MessageCallback callback) throws IOException {
        SocketChannel socketChannel = (SocketChannel)clientInfo.key.channel();
        
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
                
                callback.callback(socketChannel.getRemoteAddress(), message);
                
                socketChannel.close();
                
                clientInfo.state = ClientState.IDLE;
                clientInfo.messageLengthBuffer.clear();
                clientInfo.messageBuffer.clear();
                clientInfo.messageLength = -1;
            }
        }
    }
    
    public static void sendMessage(SocketAddress address, String message) throws IOException {
        ClientInfo clientInfo = clientInfoContainer.get(address);
        SocketChannel socketChannel = (SocketChannel)clientInfo.key.channel();
        
        byte[] messageLengthBytes = new byte[4];
        messageLengthBytes[0] = (byte)((message.length() >> 24) & 0xFF);
        messageLengthBytes[1] = (byte)((message.length() >> 16) & 0xFF);
        messageLengthBytes[2] = (byte)((message.length() >> 8) & 0xFF);
        messageLengthBytes[3] = (byte)(message.length() & 0xFF);
        
        ByteBuffer messageLengthBuffer = ByteBuffer.allocate(messageLengthBytes.length);
        messageLengthBuffer.put(messageLengthBytes);
        messageLengthBuffer.flip();
        
        socketChannel.write(messageLengthBuffer);
        
        byte[] messageBytes = new byte[message.length()];
        char[] messageChars = message.toCharArray();
        for (int i = 0; i < message.length(); i++)
        {
            messageBytes[i] = (byte)messageChars[i];
        }
        
        ByteBuffer messageBuffer = ByteBuffer.allocate(messageBytes.length);
        messageBuffer.put(messageBytes);
        messageBuffer.flip();
        
        socketChannel.write(messageBuffer);
        
        socketChannel.close();
    }
}
