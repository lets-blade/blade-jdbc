package blade.jdbc.cache;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blade.jdbc.Util;

public enum QueryCache {
	INSTANCE;

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryCache.class);

	private final boolean enabled = false;// Registry.instance().getConfiguration().cacheEnabled();

	private CacheManager cacheManager;

	// singleton
	QueryCache() {
		// cacheManager =
		// Registry.instance().getConfiguration().getCacheManager();
	}

	/**
	 * This class is a singleton, get an instance with this method.
	 *
	 * @return one and only one instance of this class.
	 */
	public static QueryCache instance() {
		return INSTANCE;
	}

	/**
	 * Adds an item to cache. Expected some lists of objects returned from
	 * "select" queries.
	 *
	 * @param tableName
	 *            - name of table.
	 * @param query
	 *            query text
	 * @param params
	 *            - list of parameters for a query.
	 * @param cache
	 *            object to cache.
	 */
	public void addItem(String tableName, String query, Object[] params, Object cache) {
		if (enabled) {
			cacheManager.addCache(tableName, getKey(tableName, query, params), cache);
		}
	}

	/**
	 * Returns an item from cache, or null if nothing found.
	 *
	 * @param tableName
	 *            name of table.
	 * @param query
	 *            query text.
	 * @param params
	 *            list of query parameters, can be null if no parameters are
	 *            provided.
	 * @return cache object or null if nothing found.
	 */
	public Object getItem(String tableName, String query, Object[] params) {

		if (enabled) {
			String key = getKey(tableName, query, params);
			Object item = cacheManager.getCache(tableName, key);
			if (item == null) {
				logAccess(query, params, "MISS");
			} else {
				logAccess(query, params, "HIT");
			}
			return item;
		} else {
			return null;
		}
	}

	static void logAccess(String query, Object[] params, String access) {
		if (LOGGER.isInfoEnabled()) {
			StringBuilder log = new StringBuilder().append(access).append(", ").append('"').append(query).append('"');
			if (!Util.empty(params)) {
				log.append(", with parameters: ").append('<');
				Util.join(log, params, ">, <");
				log.append('>');
			}
			LOGGER.debug(log.toString());
		}
	}

	private String getKey(String tableName, String query, Object[] params) {
		return tableName + query + (params == null ? null : Arrays.asList(params).toString());
	}

	/**
	 * This method purges (removes) all caches associated with a table, if
	 * caching is enabled and a corresponding model is marked cached.
	 *
	 * @param metaModel
	 *            meta-model whose caches are to purge.
	 */
	/*
	 * public void purgeTableCache(MetaModel metaModel) { if(enabled &&
	 * metaModel.cached()){ cacheManager.flush(new
	 * CacheEvent(metaModel.getTableName(), getClass().getName())); } }
	 */

	/**
	 * Use {@link #purgeTableCache(MetaModel)} whenever you can.
	 *
	 * @param tableName
	 *            name of table whose caches to purge.
	 */
	/*
	 * public void purgeTableCache(String tableName) { MetaModel mm =
	 * metaModelFor(tableName); if(mm != null && enabled && mm.cached()){
	 * cacheManager.flush(new CacheEvent(mm.getTableName(),
	 * getClass().getName())); } }
	 */

	public CacheManager getCacheManager() {
		return cacheManager;
	}
}
