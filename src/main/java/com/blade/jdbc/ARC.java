package com.blade.jdbc;

import java.util.Arrays;
import java.util.List;

import org.sql2o.Connection;
import org.sql2o.Query;

import blade.kit.EncrypKit;
import blade.kit.logging.Logger;
import blade.kit.logging.LoggerFactory;

public class ARC {

	private static final Logger LOGGER = LoggerFactory.getLogger(ARC.class);
	
	private Connection connection;
	private Object[] args;
	
	private String customSql;
	private String executeSql;
	
	private int pageIndex, pageSize;
	
	private boolean isCache;
	
	public ARC(Connection connection, String sql, boolean cache) {
		this.connection = connection;
		this.executeSql = sql;
		this.isCache = cache;
	}
	
	public ARC(Connection connection, String sql, boolean cache, Object...args) {
		this.connection = connection;
		this.args = args;
		this.customSql = sql;
		this.executeSql = sql;
		this.isCache = cache;
		for(int i=1, len = args.length; i<=len; i++){
			this.executeSql = this.executeSql.replaceFirst("\\?", ":p" + i); 
		}
	}
	
	private Query buildQuery(String sql){
		return this.buildQuery(sql, false);
	}
	
	private Query buildQuery(String sql, boolean isPage){
		Query query = connection.createQuery(sql);
		LOGGER.debug("==>  Preparing: {}", customSql);
		if(null != args && args.length > 0){
			if(isPage){
				this.pageSize = (Integer) args[args.length - 1];
				this.pageIndex = (Integer) args[args.length - 2];
				if(this.pageIndex < 1){
					args[args.length - 2] = 0;
				} else {
					int start = (this.pageIndex - 1) * this.pageSize;
					args[args.length - 2] = start;
				}
			}
			query.withParams(args);
			LOGGER.debug("==>  Parameters: {}", Arrays.toString(args));
		}
		return query;
	}
	
	private Query buildCountQuery(String countSql){
		Query query = connection.createQuery(countSql);
		LOGGER.debug("==>  Preparing: {}", countSql);
		if(null != args && args.length > 2 && this.executeSql.indexOf("where") != -1){
			Object[] ar = new Object[args.length - 2];
			System.arraycopy(args, 0, ar, ar.length, ar.length);
			query.withParams(ar);
			LOGGER.debug("==>  Parameters: {}", Arrays.toString(ar));
		}
		return query;
	}
	
	private <T> String getCacheKey(String sql, Class<T> type){
		if(null != args && args.length > 0){
			sql += Arrays.toString(args);
		}
		String tableName = "";
		if(null == type){
			tableName = ARKit.getTable(sql);
		} else {
			tableName = ARKit.tableName(type);
		}
		sql = sql.replaceAll("\\s+", "");
		return tableName + "#" + EncrypKit.md5(sql);
	}
	
	private <T> void autoAdd(OptType optType, Class<T> type){
		if(optType == OptType.QUERY){
			// 没有select * from xxx
			if(this.customSql.indexOf("select") == -1){
				String prfix = "select * from " + ARKit.tableName(type) + " ";
				this.customSql = prfix + this.customSql;
			}
			if(this.executeSql.indexOf("select") == -1){
				String prfix = "select * from " + ARKit.tableName(type) + " ";
				this.executeSql = prfix + this.executeSql;
			}
		}
	}
	
	public <T> List<T> list(Class<T> type) {
		return this.list(type, false);
	}
	
	public <T> List<T> list(Class<T> type, boolean isPage) {
		autoAdd(OptType.QUERY, type);
		List<T> result = null;
		int total = 0;
		try {
			if(isCache){
				String searchKey = this.getCacheKey(this.executeSql, type);
				List<T> list_cache = DB.cache.get(searchKey);
				if(null != list_cache){
					result = list_cache;
				} else {
					Query query = buildQuery(this.executeSql, isPage);
					result = query.executeAndFetch(type);
					DB.cache.set(searchKey, result);
					if(null != result){
						total = result.size();
					}
					LOGGER.debug("<==  Total: {}", total);
				}
			} else {
				Query query = buildQuery(this.executeSql, isPage);
				result = query.executeAndFetch(type);
				if(null != result){
					total = result.size();
				}
				LOGGER.debug("<==  Total: {}", total);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(false);
		}
		return result;
	}
	
	public <T> Page<T> page(Class<T> type) {
		autoAdd(OptType.QUERY, type);
		Long rows = 0L;
		String countSql = this.getCountSql(this.executeSql);
		if(isCache){
			this.pageSize = (Integer) args[args.length - 1];
			this.pageIndex = (Integer) args[args.length - 2];
			String countKey = this.getCacheKey(countSql, type);
			Long rows_cache = DB.cache.get(countKey);
			if(null != rows_cache){
				rows = rows_cache;
			} else {
				Query queryCount = buildCountQuery(countSql);
				rows = queryCount.executeAndFetchFirst(Long.class);
				DB.cache.set(countKey, rows);
				LOGGER.debug("<==  Total: {}", rows);
			}
		} else {
			Query queryCount = buildCountQuery(countSql);
			rows = queryCount.executeAndFetchFirst(Long.class);
			LOGGER.debug("<==  Total: {}", rows);
		}
		
		List<T> result = this.list(type, true);
		Page<T> pageResult = new Page<T>(rows, pageIndex, pageSize);
		try {
			pageResult.setResults(result);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(false);
		}
		return pageResult;
	}
	
	public <T> T first(Class<T> type) {
		autoAdd(OptType.QUERY, type);
		T result = null;
		int total = 0;
		try {
			if(isCache){
				String searchKey = this.getCacheKey(this.executeSql, type);
				T result_cache = DB.cache.get(searchKey);
				if(null != result_cache){
					result = result_cache;
				} else {
					Query query = this.buildQuery(this.executeSql);
					result = query.executeAndFetchFirst(type);
					DB.cache.set(searchKey, result);
					LOGGER.debug("<==  Total: {}", total);
				}
			} else {
				Query query = this.buildQuery(this.executeSql);
				result = query.executeAndFetchFirst(type);
				LOGGER.debug("<==  Total: {}", total);
			}
			if(null != result){
				total = 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(false);
		}
		return result;
	}
	
	public long count() {
		Long result = 0L;
		try {
			if(isCache){
				String countKey = this.getCacheKey(this.executeSql, null);
				Long result_cache = DB.cache.get(countKey);
				if(null != result_cache){
					result = result_cache;
				} else {
					Query query = this.buildQuery(this.executeSql);
					result = query.executeAndFetchFirst(Long.class);
					DB.cache.set(countKey, result);
					LOGGER.debug("<==  Total: {}", result);
				}
			} else {
				Query query = this.buildQuery(this.executeSql);
				result = query.executeAndFetchFirst(Long.class);
				LOGGER.debug("<==  Total: {}", result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(false);
		}
		return result;
	}
	
	private String getCountSql(String sql){
		sql = sql.replaceFirst("\\*", "count(1)");
		int pos = sql.indexOf("order by");
		if(pos != -1){
			return sql.substring(0, pos);
		}
		pos = sql.indexOf("limit");
		if(pos != -1){
			return sql.substring(0, pos);
		}
		return sql;
	}
	
	public ARC cache(boolean isCache){
		this.isCache = isCache;
		return this;
	}
	
	public Connection next() {
		Query query = this.buildQuery(this.executeSql);
		Connection connection = query.executeUpdate();
		return connection;
	}
	
	public int commit() {
		try {
			int result = this.next().getResult();
			if(isCache){
				// refresh the cache
				String table = ARKit.getTable(this.executeSql);
				DB.cache.clean(table);
			}
			return result;
		} catch (Exception e) {
			if(null != connection){
				connection.rollback();
			}
			e.printStackTrace();
		} finally {
			close(true);
		}
		return 0;
	}
	
	public Object key() {
		try {
			Query query = this.buildQuery(this.executeSql);
			Object result = query.executeUpdate().getKey();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(true);
		}
		return null;
	}
	
	private void close(boolean isCommit){
		if(null != connection){
			if(isCommit){
				connection.commit();
			}
			connection.close();
		}
	}
	
}