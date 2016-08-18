package blade.jdbc.cache;
public interface CacheEventListener {
    void onFlush(CacheEvent event);
}
