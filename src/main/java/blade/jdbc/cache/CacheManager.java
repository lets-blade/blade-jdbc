package blade.jdbc.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CacheManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheManager.class);

    List<CacheEventListener> listeners = new ArrayList<CacheEventListener>();

    /**
     * Returns a cached item. Can return null if not found.
     * @param group group of caches - this is a name of a table for which query results are cached
     * @param key key of the item.
     * @return a cached item. Can return null if not found.
     */
    public abstract Object getCache(String group, String key);

    /**
     * Adds item to cache.
     *
     * @param group group name of cache.
     * @param key key of the item.
     * @param cache cache item to add to cache.
     */
    public abstract void addCache(String group, String key, Object cache);


    public abstract void doFlush(CacheEvent event);


    /**
     * Flashes cache.
     *
     * @param propagate true to propagate event to listeners, false to not propagate
     * @param event type of caches to flush.
     */
    public final void flush(CacheEvent event, boolean propagate){
        doFlush(event);
        if(propagate){
            propagate(event);
        }

        if (LOGGER.isInfoEnabled()) {
            String message = "Cache purged: " + (event.getType() == CacheEvent.CacheEventType.ALL
                    ? "all caches" : "table: " + event.getGroup());
            LOGGER.debug(message);
        }
    }

    private void propagate(CacheEvent event){
        for(CacheEventListener listener: listeners){
            try{
                listener.onFlush(event);
            }catch(Exception e){
                LOGGER.warn("failed to propagate cache event: {} to listener: {}", event, listener, e);
            }
        }
    }


    /**
     * Flashes cache.
     *
     * @param event type of caches to flush.
     */
    public final void flush(CacheEvent event){
        flush(event, true);
    }

    public final void addCacheEventListener(CacheEventListener listener){
        listeners.add(listener);
    }

    public final void removeCacheEventListener(CacheEventListener listener){
        listeners.remove(listener);
    }

    public final void removeAllCacheEventListeners(){
        listeners = new ArrayList<CacheEventListener>();
    }

    /**
     * This method purges (removes) all caches associated with a table, if caching is enabled and
     * a corresponding model is marked cached.
     *
     * @param metaModel meta-model whose caches are to purge.
     */
    /*public void purgeTableCache(MetaModel metaModel) {
        flush(new CacheEvent(metaModel.getTableName(), getClass().getName()));
    }*/

    /**
     * Use {@link #purgeTableCache(MetaModel)} whenever you can.
     *
     * @param tableName name of table whose caches to purge.
     */
    public void purgeTableCache(String tableName) {
        flush(new CacheEvent(tableName, getClass().getName()));
    }


    /**
     * Generates a cache key. Subclasses may override this implementation.
     *
     * @param tableName name of a table
     * @param query query
     * @param params query parameters.
     * @return generated key for tied to these parameters.
     */
    public String getKey(String tableName, String query, Object[] params) {
        return tableName + query + (params == null ? null : Arrays.asList(params).toString());
    }
}