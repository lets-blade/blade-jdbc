package com.blade.jdbc;

import java.util.List;

import org.sql2o.Connection;
import org.sql2o.Query;

import blade.kit.logging.Logger;
import blade.kit.logging.LoggerFactory;

public class ARC {

	private static final Logger LOGGER = LoggerFactory.getLogger(ARC.class);
	
	private Connection connection;
	private Object[] args;
	
	private String executeSql;
	
	public ARC(Connection connection, String sql) {
		this.connection = connection;
		this.executeSql = sql;
	}
	
	public ARC(Connection connection, String sql, Object...args) {
		this.connection = connection;
		this.args = args;
		this.executeSql = sql;
		
		for(int i=1, len = args.length; i<=len; i++){
			this.executeSql = this.executeSql.replaceFirst("\\?", ":p" + i); 
		}
		
	}
	
	private Query buildQuery(){
		
		Query query = connection.createQuery(executeSql);
		
		LOGGER.info("execute sql: {}", executeSql);
		if(null != args && args.length > 0){
			query.withParams(args);
		}
		
		return query;
	}
	
	private <T> void autoAdd(OptType optType, Class<T> type){
		if(optType == OptType.QUERY){
			// 没有select * from xxx
			if(this.executeSql.indexOf("select") == -1){
				String prfix = "select * from " + ARKit.tableName(type) + " ";
				this.executeSql = prfix + this.executeSql;
			}
		}
	}
	
	public <T> List<T> list(Class<T> type) {
		
		autoAdd(OptType.QUERY, type);
		
		Query query = buildQuery();
		List<T> result = query.executeAndFetch(type);
		close(false);
		return result;
	}
	
	public <T> T first(Class<T> type) {
		
		autoAdd(OptType.QUERY, type);
		
		Query query = buildQuery();
		T result = query.executeAndFetchFirst(type);
		close(false);
		return result;
	}
	
	public Connection next() {
		Query query = buildQuery();
		return query.executeUpdate();
	}
	
	public int commit() {
		try {
			Query query = buildQuery();
			int result = query.executeUpdate().getResult();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(true);
		}
		return 0;
	}
	
	public Object key() {
		try {
			Query query = buildQuery();
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