package blade.jdbc.cache;
public class CacheEvent {

    public static final CacheEvent ALL = new CacheEvent(null);


    public enum  CacheEventType{
        /**
         * This type means that all caches need to be cleared. 
         */
        ALL,

        /**
         * This type means that only a cache for a specific group (table) needs to be cleared.
         */
        GROUP
    }
    private String source, group;
    private CacheEventType type;


    /**
     * Creates a new event type of {@link org.javalite.activejdbc.cache.CacheEvent.CacheEventType#GROUP}.
     * Usually an application creates an instance of this event to clear a group of caches for a table.
     *
     *
     * @param group name of group (usually name of table), cannot be null.
     * @param source string representation of source of event, whatever that means for the application. This event will
     * be broadcast to listeners, and they might use this piece of information. Can be null. 
     */
    public CacheEvent( String group, String source){
        if(group == null)
            throw new IllegalArgumentException("group cannot be null");
        
        this.type = CacheEventType.GROUP;
        this.source = source;
        this.group = group;
    }


    /**
     * Creates a new event type of {@link org.javalite.activejdbc.cache.CacheEvent.CacheEventType#ALL}
     *
     * @param source string representation of source of event, whatever that means for the application. This event will
     * be broadcast to listeners, and they might use this piece of information. Can be null. 
     */
    public CacheEvent(String source){
        this.type = CacheEventType.ALL;
        this.source = source;
    }


    public String getSource() {
        return source;
    }

    public CacheEventType getType() {
        return type;
    }

    public String getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return "CacheEvent{" +
                "source='" + source + '\'' +
                ", group='" + group + '\'' +
                ", type=" + type +
                '}';
    }
}