/**
 * Copyright (c) 2016, biezhi 王爵 (biezhi.me@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blade.jdbc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.sql2o.Connection;
import org.sql2o.Query;

import blade.kit.EncrypKit;
import blade.kit.logging.Logger;
import blade.kit.logging.LoggerFactory;

/**
 * ARC 根据sql语句来操作数据库，它具有原子性，每一次sql的执行都会生成一个ARC对象。
 *
 * @author	<a href="mailto:biezhi.me@gmail.com" target="_blank">biezhi</a>
 * @since	1.0
 */
public class ARC {

	private static final Logger LOGGER = LoggerFactory.getLogger(ARC.class);
	
	// 当前数据库连接对象
	private Connection connection;
	
	// 你的参数列表
	private Object[] args;
	
	// 你的where条件参数列表
	private Object[] whereArgs;
	
	// 要执行的sql语句
	private String executeSql;
	
	// 本次操作是否开启缓存
	private boolean isCache;
	
	public ARC(Connection connection, String sql, boolean cache) {
		this.connection = connection;
		this.executeSql = sql;
		this.isCache = cache;
	}
	
	public ARC(Connection connection, String sql, boolean cache, Object...args) {
		this.connection = connection;
		this.executeSql = sql;
		this.isCache = cache;
		
		this.args = args;
		
		List<Object> whereList = new ArrayList<Object>();
		for(int i=0, len = args.length; i<len; i++){
			int pos = this.executeSql.indexOf("?");
			if(pos == -1){
				break;
			}
			String prefix = this.executeSql.substring(0, pos).trim();
			
			// 找到倒数第一个字符
			int lastSpace = prefix.lastIndexOf(' ');
			String postion = prefix.substring(lastSpace).trim();
			
			if(ARKit.WHEREPOS.contains(postion)){
				whereList.add(args[i]);
			}
			this.executeSql = this.executeSql.replaceFirst("\\?", ":p" + (i+1)); 
		}
		this.whereArgs = whereList.toArray();
	}
	
	/**
	 * 构建一个查询的Query对象
	 * @param sql	sql语句
	 * @return		返回Query对象
	 */
	private Query buildQuery(String sql){
		Query query = connection.createQuery(sql);
		LOGGER.debug("==>  Preparing: {}", sql);
		if(null != args && args.length > 0){
			query.withParams(args);
			LOGGER.debug("==>  Parameters: {}", Arrays.toString(args));
		}
		return query;
	}
	
	/**
	 * 构建一个查询记录数的Query对象
	 * @param sql	sql语句
	 * @return		返回Query对象
	 */
	private Query buildCountQuery(String sql){
		String countSql = this.getCountSql(sql);
		Query query = connection.createQuery(countSql);
		LOGGER.debug("==>  Preparing: {}", countSql);
		if(null != args && args.length > 2 && sql.indexOf("where") != -1){
			int len = args.length - 2;
			if(sql.indexOf("order by") != -1){
				len -= 1;
			}
			Object[] ar = new Object[len];
			System.arraycopy(args, 0, ar, 0, len);
			query.withParams(ar);
			LOGGER.debug("==>  Parameters: {}", Arrays.toString(ar));
		}
		return query;
	}
	
	/**
	 * 获取表名作为缓存key
	 * @param sql		sql语句
	 * @param type		数据库对应的实体Class
	 * @return			返回表名
	 */
	private <T> String getCacheKey(String sql, Class<T> type){
		String tableName = "";
		
		if(null != type && type.getSuperclass().equals(Serializable.class) && !ARKit.isBasicType(type)){
			tableName = ARKit.tableName(type);
		} else {
			tableName = ARKit.getTable(sql);
		}
		return tableName;
	}
	
	/**
	 * 获取缓存字段值，这里使用了MD5加密存储
	 * @param sql	sql语句
	 * @return		返回加密后的缓存值
	 */
	private <T> String getCacheField(String sql){
		if(null != args && args.length > 0){
			sql += Arrays.toString(args);
		}
		sql = sql.replaceAll("\\s+", "");
		return EncrypKit.md5(sql);
	}
	
	/**
	 * 获取缓存字段值，这里使用了MD5加密存储
	 * @param sql	sql语句
	 * @return		返回加密后的缓存值
	 */
	private <T> String getCountCacheField(String sql){
		if(null != whereArgs && whereArgs.length > 0){
			sql += Arrays.toString(whereArgs);
		}
		sql = sql.replaceAll("\\s+", "");
		return EncrypKit.md5(sql);
	}
	
	// 自动弥补查询语句中没有写 select * from
	private <T> void autoAdd(Class<T> type){
		// 没有select * from xxx
		if(this.executeSql.indexOf("select") == -1 && !ARKit.isBasicType(type)){
			String prfix = "select * from " + ARKit.tableName(type) + " ";
			this.executeSql = prfix + this.executeSql;
		}
	}
	
	public <T extends Serializable> List<T> list(Class<T> type) {
		return this.list(type, false);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> map() {
		Map<String,Object> result = this.buildQuery(this.executeSql).executeScalar(Map.class);
		long total = (null != result) ? 1 : 0;
		LOGGER.debug("<==  Total: {}", total);
		this.close(false);
		return result;
	}
	
	public List<Map<String,Object>> listmap() {
		List<Map<String,Object>> result = this.buildQuery(this.executeSql).executeAndFetchTable().asList();
		long total = 0L;
		if(null != result){
			total = result.size();
		}
		LOGGER.debug("<==  Total: {}", total);
		this.close(false);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Serializable> List<T> list(Class<T> type, boolean isPage) {
		if(!ARKit.isBasicType(type)){
			autoAdd(type);
		}
		
		List<T> result = null;
		int total = 0;
		try {
			
			if(isCache){
				
				String cacheKey = this.getCacheKey(this.executeSql, type) + "_list";
				String cacheField = this.getCacheField(this.executeSql);
				
				List<Serializable> list_cache = DB.cache.hget(cacheKey, cacheField);
				
				if(null != list_cache){
					if(list_cache.size() > 0){
						if(!ARKit.isBasicType(type)){
							result = new ArrayList<T>(list_cache.size());
							for (Serializable id : list_cache) {
								T t = AR.findById(type, id);
								if(null != t){
									result.add(t);
								}
			                }
						} else {
							result = (List<T>) list_cache;
						}
					}
				} else {
					
					Query query = null;
					if(!ARKit.isBasicType(type)){
						String pkName = ARKit.pkName(type);
						String fidSql = this.executeSql.replaceFirst("\\*", pkName);
						query = this.buildQuery(fidSql);
						
						List<String> ids = query.executeScalarList(String.class);
						if(null != ids){
							total = ids.size();
							LOGGER.debug("<==  Total: {}", total);
							
							if(total > 0){
								result = new ArrayList<T>(total);
								for (String id : ids) {
									T t = AR.findById(type, id);
									if(null != t){
										result.add(t);
									}
				                }
								DB.cache.hset(cacheKey, cacheField, ids);
							}
						}
						
					} else {
						query = this.buildQuery(this.executeSql);
						result = query.executeScalarList(type);
						if(null != result){
							total = result.size();
							LOGGER.debug("<==  Total: {}", total);
							if(total > 0){
								DB.cache.hset(cacheKey, cacheField, result);
							}
						}
					}
				}
			} else {
				Query query = this.buildQuery(this.executeSql);
				if(!ARKit.isBasicType(type)){
					result = query.executeAndFetch(type);
				} else {
					result = query.executeScalarList(type);
				}
				
				if(null != result){
					total = result.size();
				}
				LOGGER.debug("<==  Total: {}", total);
			}
			this.close(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public <T extends Serializable> Page<T> page(Class<T> type) {
		
		autoAdd(type);
		
		long rows = this.count(this.executeSql);
		
		int page = 1, pageIndex = 0, pageSize = 0;
		if(null != args && args.length > 1){
			pageSize = (Integer) args[args.length - 1];
			page = (Integer) args[args.length - 2];
			
			pageIndex = (page - 1) * pageSize;
			args[args.length - 2] = pageIndex;
		}
		
		List<T> result = this.list(type, true);
		
		Page<T> pageResult = new Page<T>(rows, page, pageSize);
		try {
			pageResult.setResults(result);
			this.close(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pageResult;
	}
	
	public <T extends Serializable> T first(Class<T> type) {
		
		if(!ARKit.isBasicType(type)){
			autoAdd(type);
		}
		
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
					if(!ARKit.isBasicType(type)){
						result = query.executeAndFetchFirst(type);
					} else {
						result = query.executeScalar(type);
					}
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
				if(!ARKit.isBasicType(type)){
					result = query.executeAndFetchFirst(type);
				} else {
					result = query.executeScalar(type);
				}
				if(null != result){
					total = 1;
				}
				LOGGER.debug("<==  Total: {}", total);
			}
			this.close(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public long count() {
		return this.count(this.executeSql);
	}
	
	public long count(String sql) {
		
		Long result = 0L;
		
		try {
			
			String countSql = ARKit.cleanCountSql(sql);
			
			if(isCache){
				String cacheKey = this.getCacheKey(countSql, null) + "_count";
				String cacheField = this.getCountCacheField(countSql);
				
				Long result_cache = DB.cache.hget(cacheKey, cacheField);
				
				if(null != result_cache){
					result = result_cache;
				} else {
					
					Query query = this.buildCountQuery(countSql);
					
					result = query.executeAndFetchFirst(Long.class);
					if(null != result){
						DB.cache.hset(cacheKey, cacheField, result);
					}
					LOGGER.debug("<==  Total: {}", result);
				}
			} else {
				Query query = this.buildCountQuery(countSql);
				result = query.executeAndFetchFirst(Long.class);
				LOGGER.debug("<==  Total: {}", result);
			}
			this.close(false);
		} catch (Exception e) {
			e.printStackTrace();
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
		return this.executeUpdate(false);
	}
	
	public int executeUpdate(boolean delCache) {
		Connection connection = this.next(delCache);
		if(null != connection){
			connection.commit();
			return connection.getResult();
		}
		return 0;
	}
	
	public Connection next() {
		return this.next(false);
	}
	
	public Connection next(boolean delCache) {
		try {
			Query query = this.buildQuery(this.executeSql);
			Connection connection = query.executeUpdate();
			int result = connection.getResult();
			
			if(isCache && result > 0){
				
				String table = ARKit.getTable(this.executeSql);
				if(this.executeSql.indexOf("insert") != -1){
					DB.cache.hdel(table + "_list");
					DB.cache.hdel(table + "_count");
					DB.cache.hdel(table + "_detail");
					
					LOGGER.debug("update cache:{}", table);
					
				} else if(this.executeSql.indexOf("update") != -1){
					DB.cache.hdel(table + "_list");
					DB.cache.hdel(table + "_detail");
					if(delCache){
						DB.cache.hdel(table + "_count");
					}
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
			/*if(null != connection){
				connection.rollback();
			}*/
			LOGGER.error(e.getMessage(), e);
		}
		return connection;
	}
	
	public Object key() {
		try {
			Query query = this.buildQuery(this.executeSql);
			Object result = query.executeUpdate().getKey();
			if(isCache && null != result){
				String table = ARKit.getTable(this.executeSql);
				if(this.executeSql.indexOf("insert") != -1){
					DB.cache.hdel(table + "_list");
					DB.cache.hdel(table + "_count");
					DB.cache.hdel(table + "_detail");
					LOGGER.debug("update cache:{}", table);
				}
			}
			this.close(true);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
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