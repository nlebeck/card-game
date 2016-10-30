package niellebeck.cardgameserver.messaging;

import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Container for ClientInfo objects that allows indexing them by SocketAddress
 * and by SelectionKey.
 */
public class ClientInfoContainer {
    private Set<ClientInfo> infoList;
    private Map<SocketAddress, ClientInfo> addressInfoMap;
    private Map<SelectionKey, ClientInfo> keyInfoMap;
    
    public ClientInfoContainer() {
        infoList = new HashSet<ClientInfo>();
        addressInfoMap = new HashMap<SocketAddress, ClientInfo>();
        keyInfoMap = new HashMap<SelectionKey, ClientInfo>();
    }
    
    public void add(ClientInfo info) {
        if (addressInfoMap.containsKey(info.address)) {
            throw new RuntimeException("Cannot add client: a client with the same remote address already exists");
        }
        else if (keyInfoMap.containsKey(info.key)) {
            throw new RuntimeException("Cannot add client: a client with the same selection key already exists");
        }
        
        infoList.add(info);
        addressInfoMap.put(info.address, info);
        keyInfoMap.put(info.key, info);
    }
    
    public void remove(ClientInfo info) {
        infoList.remove(info);
        addressInfoMap.remove(info.address, info);
        keyInfoMap.remove(info.key, info);
    }
    
    public ClientInfo get(SocketAddress address) {
        return addressInfoMap.get(address);
    }
    
    public ClientInfo get(SelectionKey key) {
        return keyInfoMap.get(key);
    }
    
    public boolean containsSelectionKey(SelectionKey key) {
        return keyInfoMap.containsKey(key);
    }
    
    public boolean containsSocketAddress(SocketAddress address) {
        return addressInfoMap.containsKey(address);
    }
}
