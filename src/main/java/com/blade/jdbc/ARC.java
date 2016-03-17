package com.blade.jdbc;

import java.io.Serializable;
import java.util.ArrayList;
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
		LOGGER.debug("==>  Preparing: {}", sql);
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
	
	private Query buildCountQuery(String sql){
		String countSql = this.getCountSql(sql);
		Query query = connection.createQuery(countSql);
		LOGGER.debug("==>  Preparing: {}", countSql);
		if(null != args && args.length > 2 && this.executeSql.indexOf("where") != -1){
			int len = args.length - 2;
			if(this.executeSql.indexOf("order by") != -1){
				len -= 1;
			}
			Object[] ar = new Object[len];
			System.arraycopy(args, 0, ar, 0, len);
			query.withParams(ar);
			LOGGER.debug("==>  Parameters: {}", Arrays.toString(ar));
		}
		return query;
	}
	
	private <T> String getCacheKey(String sql, Class<T> type){
		String tableName = "";
		if(null == type){
			tableName = ARKit.getTable(sql);
		} else {
			tableName = ARKit.tableName(type);
		}
		return tableName;
	}
	
	private <T> String getCacheField(String sql){
		if(null != args && args.length > 0){
			sql += Arrays.toString(args);
		}
		sql = sql.replaceAll("\\s+", "");
		return EncrypKit.md5(sql);
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
	
	public <T extends Serializable> List<T> list(Class<T> type) {
		return this.list(type, false);
	}
	
	public <T extends Serializable> List<T> list(Class<T> type, boolean isPage) {
		autoAdd(OptType.QUERY, type);
		List<T> result = null;
		int total = 0;
		try {
			if(isCache){
				String cacheKey = this.getCacheKey(this.executeSql, type) + "_list";
				String cacheField = this.getCacheField(this.executeSql);
				
				List<Long> list_cache = DB.cache.hget(cacheKey, cacheField);
				
				if(null != list_cache){
					if(list_cache.size() > 0){
						result = new ArrayList<T>(list_cache.size());
						for (Serializable id : list_cache) {
							T t = AR.findById(type, id);
							if(null != t){
								result.add(t);
							}
		                }
					}
				} else {
					
					String pkName = ARKit.pkName(type);
					String fidSql = this.executeSql.replaceFirst("\\*", pkName);
					Query query = buildQuery(fidSql, isPage);
					
					List<Long> ids = query.executeScalarList(Long.class);
					if(null != ids){
						total = ids.size();
						LOGGER.debug("<==  Total: {}", total);
						
						if(total > 0){
							result = new ArrayList<T>(total);
							for (Serializable id : ids) {
								T t = AR.findById(type, id);
								if(null != t){
									result.add(t);
								}
			                }
							DB.cache.hset(cacheKey, cacheField, ids);
						}
					}
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
	
	public <T extends Serializable> Page<T> page(Class<T> type) {
		autoAdd(OptType.QUERY, type);
		
		long rows = this.count(this.executeSql);
		
		LOGGER.debug("<==  Total: {}", rows);
		
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
	
	public <T extends Serializable> T first(Class<T> type) {
		autoAdd(OptType.QUERY, type);
		T result = null;
		int total = 0;
		try {
			if(isCache){
				
				String cacheKey = this.getCacheKey(this.executeSql, type) + "_detail";
				String cacheField = this.getCacheField(this.executeSql);
				
				T result_cache = DB.cache.hget(cacheKey, cacheField);
				if(null != result_cache){
					result = result_cache;
				} else {
					Query query = this.buildQuery(this.executeSql);
					result = query.executeAndFetchFirst(type);
					if(null != result){
						DB.cache.hset(cacheKey, cacheField, result);
					}
					if(null != result){
						total = 1;
					}
					LOGGER.debug("<==  Total: {}", total);
				}
			} else {
				Query query = this.buildQuery(this.executeSql);
				result = query.executeAndFetchFirst(type);
				if(null != result){
					total = 1;
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
	
	public long count() {
		return this.count(this.executeSql);
	}
	
	public long count(String sql) {
		Long result = 0L;
		try {
			if(isCache){
				
				String cacheKey = this.getCacheKey(sql, null) + "_count";
				String cacheField = this.getCacheField(sql);
				
				Long result_cache = DB.cache.hget(cacheKey, cacheField);
				
				if(null != result_cache){
					result = result_cache;
				} else {
					
					Query query = this.buildCountQuery(sql);
					
					result = query.executeAndFetchFirst(Long.class);
					if(null != result){
						DB.cache.hset(cacheKey, cacheField, result);
					}
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
	
	public int executeUpdate() {
		Connection connection = this.next();
		if(null != connection){
			connection.commit();
			return connection.getResult();
		}
		return 0;
	}
	
	public Connection next() {
		try {
			Query query = this.buildQuery(this.executeSql);
			Connection connection = query.executeUpdate();
			int result = connection.getResult();
			
			if(isCache && result > 0){
				
				String table = ARKit.getTable(this.executeSql);
				if(this.executeSql.indexOf("insert") != -1){
					DB.cache.hdel(table + "_list");
					DB.cache.hdel(table + "_count");
					
					LOGGER.debug("update cache:{}", table);
					
				} else if(this.executeSql.indexOf("update") != -1){
					DB.cache.hdel(table + "_list");
					DB.cache.hdel(table + "_detail");
					
					LOGGER.debug("update cache:{}", table);
				} else if(this.executeSql.indexOf("delete") != -1){
					DB.cache.hdel(table + "_list");
					DB.cache.hdel(table + "_detail");
					DB.cache.hdel(table + "_count");
					
					LOGGER.debug("update cache:{}", table);
				}
			}
			
			LOGGER.debug("<==  Total: {}", result);
			
			return connection;
		} catch (Exception e) {
			if(null != connection){
				connection.rollback();
			}
			e.printStackTrace();
		}
		return connection;
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