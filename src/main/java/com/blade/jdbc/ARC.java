package com.blade.jdbc;

import java.util.Arrays;
import java.util.List;

import org.sql2o.Connection;
import org.sql2o.Query;

import blade.kit.logging.Logger;
import blade.kit.logging.LoggerFactory;

public class ARC {

	private static final Logger LOGGER = LoggerFactory.getLogger(ARC.class);
	
	private Connection connection;
	private Object[] args;
	
	private String customSql;
	private String executeSql;
	
	private int pageIndex, pageSize;
	
	public ARC(Connection connection, String sql) {
		this.connection = connection;
		this.executeSql = sql;
	}
	
	public ARC(Connection connection, String sql, Object...args) {
		this.connection = connection;
		this.args = args;
		this.customSql = sql;
		this.executeSql = sql;
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
				
				int start = (this.pageIndex - 1) * this.pageSize;
				args[args.length - 2] = start;
				
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
		if(null != args && args.length > 2){
			Object[] ar = new Object[args.length - 2];
			System.arraycopy(args, 0, ar, ar.length, ar.length);
			query.withParams(ar);
			LOGGER.debug("==>  Parameters: {}", Arrays.toString(ar));
		}
		return query;
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
		Query query = buildQuery(this.executeSql, isPage);
		List<T> result = null;
		try {
			result = query.executeAndFetch(type);
			int total = 0;
			if(null != result){
				total = result.size();
			}
			LOGGER.debug("<==  Total: {}", total);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(false);
		}
		return result;
	}
	
	public <T> Page<T> page(Class<T> type) {
		autoAdd(OptType.QUERY, type);
		Query queryCount = buildCountQuery(this.executeSql);
		long rows = queryCount.executeAndFetchFirst(Long.class);
		LOGGER.debug("<==  Total: {}", rows);
		
		Page<T> pageResult = new Page<T>(rows, pageIndex, pageSize);
		List<T> result = this.list(type, true);
		try {
			pageResult.setResults(result);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(false);
		}
		return pageResult;
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
	
	public <T> T first(Class<T> type) {
		autoAdd(OptType.QUERY, type);
		Query query = this.buildQuery(this.executeSql);
		T result = null;
		try {
			result = query.executeAndFetchFirst(type);
			int total = 0;
			if(null != result){
				total = 1;
			}
			LOGGER.debug("<==  Total: {}", total);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(false);
		}
		return result;
	}
	
	public long count() {
		Query query = this.buildQuery(this.executeSql);
		long result = 0;
		try {
			result = query.executeAndFetchFirst(Long.class);
			LOGGER.debug("<==  Total: {}", result);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(false);
		}
		return result;
	}
	
	public Connection next() {
		Query query = this.buildQuery(this.executeSql);
		return query.executeUpdate();
	}
	
	public int commit() {
		try {
			Query query = this.buildQuery(this.executeSql);
			int result = query.executeUpdate().getResult();
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