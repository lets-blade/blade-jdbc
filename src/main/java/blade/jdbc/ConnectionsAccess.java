package blade.jdbc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blade.jdbc.exception.InternalException;

public class ConnectionsAccess {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionsAccess.class);
    
    private static final ThreadLocal<HashMap<String, Connection>> connectionsTL = new ThreadLocal<HashMap<String,Connection>>();
    
    private ConnectionsAccess() {
    }
    
    static Map<String, Connection> getConnectionMap(){
        if (connectionsTL.get() == null)
            connectionsTL.set(new HashMap<String, Connection>());
        return connectionsTL.get();
    }

    static Connection getConnection(String dbName){
        return getConnectionMap().get(dbName);
    }
    
    static void attach(String dbName, Connection connection, String extraInfo) {
        if(ConnectionsAccess.getConnectionMap().get(dbName) != null){
            throw new InternalException("You are opening a connection " + dbName + " without closing a previous one. Check your logic. Connection still remains on thread: " + ConnectionsAccess.getConnectionMap().get(dbName));
        }
        ConnectionsAccess.getConnectionMap().put(dbName, connection);
        LOGGER.debug("Attached connection: {} named [{}] to current thread. Extra info: {}", connection, dbName, extraInfo);
    }
    
    static void detach(String dbName){
    	LOGGER.debug("Detached connection: {} from current thread", dbName);
        getConnectionMap().remove(dbName);
    }
    
    static List<Connection> getAllConnections(){
        return new ArrayList<Connection>(getConnectionMap().values());
    }
}