package com.blade.jdbc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Query;

import com.blade.jdbc.annotation.Table;

import blade.kit.Assert;

public class ARC<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ARC.class);
	
	private Class<T> type;
	private String tableName;
	private OptType optType;
	private Set<String> whereF;
	private Set<String> setF;
	
	private Map<String, Object> setParamValues;
	private Map<String, Object> whereParamValues;
	
	private Serializable pk;
	
	private T bind;
	
	private String orderBy;
	private String groupBy;
	private StringBuffer sql;
	private Connection connection;
	
	public ARC(Class<T> type, OptType optType, Serializable pk) {
		this.type = type;
		this.optType = optType;
		this.tableName = this.getTableName(type);
		this.whereF = new LinkedHashSet<String>();
		this.setF = new LinkedHashSet<String>();
		this.setParamValues = new HashMap<String, Object>();
		this.whereParamValues = new HashMap<String, Object>();
		this.pk = pk;
	}
	
	private String getTableName(Class<T> type){
		return type.getAnnotation(Table.class).value();
	}
	
	private String getPKName(Class<T> type){
		return type.getAnnotation(Table.class).PK();
	}
	
	public List<T> list(){
		try {
			Query query = query();
			return query.executeAndFetch(type);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(null != connection){
				connection.close();
			}
		}
		return null;
	}
	
	public T first(){
		try {
			Query query = query();
			T t = query.executeAndFetchFirst(type);
			return t;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(null != connection){
				connection.close();
			}
		}
		return null;
	}
	
	public Query query(){
		
		if(setF.size() > 0){
			this.sql.append(" set");
			for(String key : setF){
				if(key.indexOf("?") != -1){
					String[] field = ARKit.getField(key);
					this.sql.append(" " + field[0] + " " + field[1] + " :" + field[0] + ",");
				} else {
					this.sql.append(" " + key + " ,");
				}
			}
			if(this.sql.indexOf(",") != -1){
				this.sql = new StringBuffer(this.sql.substring(0, sql.length() - 1));
			}
		}
		
		if(whereF.size() > 0){
			this.sql.append(" where");
			for(String key : whereF){
				// field[0] = age , field[1] = >
				if(key.indexOf("?") != -1){
					String[] field = ARKit.getField(key);
					this.sql.append(" " + field[0] + " " + field[1] + " :" + field[0] + " and");
				} else {
					this.sql.append(" " + key + " and");
				}
			}
			if(this.sql.indexOf("and") != -1){
				this.sql = new StringBuffer(this.sql.substring(0, sql.length() - 4));
			}
		}
		
		if(null != this.orderBy){
			this.sql.append(" order by " + this.orderBy);
		}
		
		if(null != this.groupBy){
			this.sql.append(" group by " + this.groupBy);
		}
		
		String sql_  = this.sql.toString();
		
		Query query = connection.createQuery(sql_);
		
		LOGGER.info("execute sql: {}", sql_);
		
		List<Object> temp = new ArrayList<Object>();
		
		if(null != this.setParamValues && this.setParamValues.size() > 0){
			Set<String> fields = setParamValues.keySet();
			for(String fieldSql : fields){
				String[] field = ARKit.getField(fieldSql);
				Object val = setParamValues.get(fieldSql);
				query.addParameter(field[0], val);
				temp.add(val);
			}
		}
		
		if(null != this.whereParamValues && this.whereParamValues.size() > 0){
			Set<String> fields = whereParamValues.keySet();
			for(String fieldSql : fields){
				String[] field = ARKit.getField(fieldSql);
				Object val = whereParamValues.get(fieldSql);
				query.addParameter(field[0], val);
				temp.add(val);
			}
		}
		
		if(null != bind){
			query.bind(bind);
		}

		if(temp.size() > 0){
			LOGGER.info("param values: {}", temp.toString());
		}
		
		temp = null;
		return query;
	}
	
	ARC<T> search(String sql) {
		this.sql = new StringBuffer("select " + sql + " from " + this.tableName);
		connection = DB.sql2o.open();
		return this;
	}
	
	ARC<T> search() {
		this.sql = new StringBuffer("select * from " + this.tableName);
		connection = DB.sql2o.open();
		return this;
	}
	
	ARC<T> update() {
		this.sql = new StringBuffer("update " + this.tableName);
		connection = DB.sql2o.beginTransaction();
		return this;
	}
	
	ARC<T> delete() {
		this.sql = new StringBuffer("delete from " + this.tableName);
		connection = DB.sql2o.beginTransaction();
		return this;
	}
	
	ARC<T> insert() {
		this.sql = new StringBuffer("insert into " + this.tableName);
		connection = DB.sql2o.beginTransaction();
		return this;
	}
	
	public ARC<T> orderBy(String orderBy) {
		this.orderBy = orderBy;
		return this;
	}
	
	public ARC<T> groupBy(String groupBy) {
		this.groupBy = groupBy;
		return this;
	}
	
	public ARC<T> where(String where) {
		Assert.notBlank(where, "The where sql not is blank.");
		this.parseWhere(where);
		return this;
	}

	public ARC<T> where(String where, Object ... args) {
		Assert.notBlank(where, "The where sql not is blank.");
		if(where.indexOf("?") != -1){
			Assert.notEmpty(args, "The where args not empty.");
			int count = ARKit.countMatches(where, "?");
			if(args.length  != count){
				throw new IllegalArgumentException("The params count don't match.");
			}
		} else {
			if(null != args && args.length > 0){
				throw new IllegalArgumentException("The params count don't match.");
			}
		}
		this.parseWhere(where, args);
		return this;
	}

	private void parseWhere(String where, Object ... args) {
		where = where.trim();
		if(null == args || args.length == 0){
			whereF.add(where);
		} else {
			int count = ARKit.countMatches(where, "?");
			List<String> keys = ARKit.split(where);
			for(int i=0; i<count; i++){
				whereF.add(keys.get(i));
				this.whereParamValues.put(keys.get(i), args[i]);
			}
		}
	}

	public T findByPk(Serializable pk) {
		if(null != pk){
			String pkName = getPKName(type);
			this.search();
			this.whereF.add(pkName + " = ?");
			this.whereParamValues.put(pkName + " = ?", pk);
			return this.first();
		}
		return null;
	}
	
	public ARC<T> set(String field, Object param) {
		Assert.notBlank(field, "Field not blank.");
		Assert.notNull(param, "Param not null.");
		
		this.setF.add(field + " = ?");
		this.setParamValues.put(field + " = ?", param);
		
		return this;
	}
	
	public int commit(){
		
		if(optType == OptType.UPDATE){
			this.update();
			if(null != pk){
				String pkName = this.getPKName(type);
				this.where(pkName + " = ?", pk);
			}
		}
		
		if(optType == OptType.DELETE){
			this.delete();
		}
		
		if(optType == OptType.INSERT){
			this.insert();
			if(null != bind){
				this.sql.append(" (");
				StringBuffer values = new StringBuffer("values (");
				List<String> fields = ARKit.getFields(bind);
				for(String field : fields){
					this.sql.append(field + ",");
					values.append(":" + field + ",");
				}
				this.sql = new StringBuffer(this.sql.substring(0, sql.length() - 1));
				values = new StringBuffer(values.substring(0, values.length() - 1));
				this.sql.append(") ").append(values.toString()).append(")");
			}
		}
		
		try {
			Query query = query();
			int result = query.executeUpdate().getResult();
			connection.commit();
			return result;
		} catch (Exception e) {
			connection.rollback();
			e.printStackTrace();
		} finally {
			if(null != connection){
				connection.close();
			}
		}
		return 0;
	}
	
	public ARC<T> bind(T bind) {
		this.bind = bind;
		return this;
	}
}
