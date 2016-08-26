package com.blade.jdbc;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blade.jdbc.dialect.Dialect;
import com.blade.jdbc.dialect.ModelInfo;
import com.blade.jdbc.exception.DBException;
import com.blade.jdbc.tx.Transaction;

public class Query {

	private static final Logger LOGGER = LoggerFactory.getLogger(Query.class);

	private Object insertRow;

	private String sql;
	private String table;
	private StringBuffer where = new StringBuffer();
	private String orderBy;
	
	private List<Object> argList = new ArrayList<Object>(10);

	private int rowsAffected;

	private DataBase db;
	private Dialect dialect;
	private Transaction transaction;
	
	private boolean cached = true;
	private String cacheKey;
	private String cacheField;
	
	public Query(DataBase db) {
		this.db = db;
		this.dialect = db.getDialect();
	}

	public <T> boolean isCache(Class<T> clazz){
		if(null != clazz){
			ModelInfo modelInfo = dialect.getModelInfo(clazz);
			if(null != modelInfo){
				return db.enableCache && modelInfo.isCached() && this.cached;
			}
		}
		return db.enableCache && this.cached;
	}
		
	public Query cached(boolean cached){
		if(!db.enableCache && cached){
			this.cached = false;
		}
		this.cached = cached;
		return this;
	}
	
	public Query where(String where, Object... args) {
		this.where.append(" and ");
		this.where.append(where);
		this.argList.addAll(Arrays.asList(args));
		return this;
	}
	
	public Query like(String key, Object value) {
		this.where(key + " like ?", value);
		return this;
	}
	
	public Query notLike(String key, Object value) {
		this.where(key + " not like ?", value);
		return this;
	}

	public Query gt(String key, Object value) {
		this.where(key + " > ?", value);
		return this;
	}

	public Query gte(String key, Object value) {
		this.where(key + " >= ?", value);
		return this;
	}

	public Query lt(String key, Object value) {
		this.where(key + " < ?", value);
		return this;
	}

	public Query lte(String key, Object value) {
		this.where(key + " <= ?", value);
		return this;
	}

	public Query eq(String key, Object value) {
		if(null != key && null != value){
			this.where(key + " = ?", value);
		}
		return this;
	}

	public Query neq(String key, Object value) {
		this.where(key + " <> ?", value);
		return this;
	}

	public Query in(String key, Object[] values) {
		String inSql = " in " + Util.getInSql(values.length);
		this.where(key + inSql, values);
		return this;
	}
	
	public Query notIn(String key, Object[] values) {
		String inSql = " not in " + Util.getInSql(values.length);
		this.where(key + inSql, values);
		return this;
	}
	
	public Query between(String key, Object value1, Object value2) {
		this.where(key + " between ? and ?", value1, value2);
		return this;
	}
	
	public Query sql(String sql, Object... args) {
		this.sql = sql;
		this.argList.addAll(Arrays.asList(args));
		return this;
	}

	public Query sql(String sql, List<Object> argList) {
		this.sql = sql;
		this.argList = argList;
		return this;
	}
	
	public String sql(){
		return this.sql;
	}
	
	public Query orderBy(String orderBy) {
		this.orderBy = orderBy;
		return this;
	}
	
	public <T> T first(Class<T> clazz) {
		return first(null, clazz);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T first(String sql, Class<T> clazz) {
		
		Connection con = null;
		PreparedStatement state = null;
		
		try {
			
			if(this.isCache(clazz)){
				ModelInfo modelInfo = dialect.getModelInfo(clazz);
				cacheKey = modelInfo.getTable() + "_rows";
				if(null == cacheField){
					cacheField = getCacheField();
				}
				Object value = db.cache.hget(cacheKey, cacheField);
				if(null != value){
					LOGGER.debug("缓存命中：key = [{}], field = [{}]", cacheKey, cacheField);
					return (T) value;
				}
			}
			
			String querySql = null != sql ? sql : dialect.getSelectSql(this, clazz);
			
			Connection localCon;
			if (transaction == null) {
				localCon = db.getConnection();
				con = localCon; // con gets closed below if non-null
			} else {
				localCon = transaction.getConnection();
			}
			
			LOGGER.debug("Preparing\t=> {}", querySql);

			state = localCon.prepareStatement(querySql);

			// load args
			Object[] args = this.loadArgs(state);
			
			if(null != args){
				LOGGER.debug("Parameters\t=> {}", Arrays.toString(args));
			}
			
			ResultSet rs = state.executeQuery();

			ResultSetMetaData meta = rs.getMetaData();
			int colCount = meta.getColumnCount();
			
			T result = null;
			
			if (Util.isPrimitiveOrString(clazz)) {
				if (rs.next()) {
					result = (T) rs.getObject(1);
				}
			} else {
				ModelInfo pojoInfo = dialect.getModelInfo(clazz);
				if (rs.next()) {
					T row = clazz.newInstance();
					for (int i = 1; i <= colCount; i++) {
						String colName = meta.getColumnName(i);
						Object colValue = rs.getObject(i);
						pojoInfo.putValue(row, colName, colValue);
					}
					result = row;
				}
			}
			if(this.isCache(clazz) && null != result){
				LOGGER.debug("缓存设置：key = [{}], field = [{}]", cacheKey, cacheField);
				db.cache.hset(cacheKey, cacheField, result, Const.CACHE_EXPIRE_TIME);
			}
			LOGGER.debug("Total\t<= {}", result == null ? 0 : 1);
			
			return result;
		} catch (InstantiationException e) {
			throw new DBException(e);
		} catch (IllegalAccessException e) {
			throw new DBException(e);
		} catch (SQLException e) {
			throw new DBException(e);
		} finally {
			ConnectionsAccess.detach(db.name());
			Util.closeQuietly(state);
			Util.closeQuietly(con);
		}
	}
	
	public <T> T findByPK(Serializable pk, Class<T> clazz) {
		ModelInfo modelInfo = dialect.getModelInfo(clazz);
		String pkField = modelInfo.getPrimaryKeyName();
		this.eq(pkField, pk);
		if(isCache(clazz)){
			cacheField = pk.toString();
		}
		return this.first(clazz);
	}
	
	public <T> long count(Class<T> clazz){
		
		long count = 0;
		
		Connection con = null;
		PreparedStatement state = null;
		
		try {
			
			String querySql = dialect.getCountSql(this, clazz);
			
			/**
			 * if enabled cache
			 */
			if(this.isCache(clazz)){
				if(null == cacheKey){
					cacheKey = dialect.getModelInfo(clazz).getTable() + "_count";
				}
				if(null == cacheField){
					cacheField = getCacheField();
				}
				Object value = db.cache.hget(cacheKey, cacheField);
				if(null != value){
					LOGGER.debug("缓存命中：key = [{}], field = [{}]", cacheKey, cacheField);
					return (Long) value;
				}
			}
			
			Connection localCon;
			if (transaction == null) {
				localCon = db.getConnection();
				con = localCon; // con gets closed below if non-null
			} else {
				localCon = transaction.getConnection();
			}
			
			LOGGER.debug("Preparing\t=> {}", querySql);

			state = localCon.prepareStatement(querySql);
			
			// load args
			Object[] args = this.loadArgs(state);
			if(null != args){
				LOGGER.debug("Parameters\t=> {}", Arrays.toString(args));
			}
			
			ResultSet rs = state.executeQuery();
			if(rs.next()){
				count = rs.getLong(1);
			}
			LOGGER.debug("Total\t<= {}", count);
			
			if(this.isCache(clazz)){
				LOGGER.debug("缓存设置：key = [{}], field = [{}]", cacheKey, cacheField);
				db.cache.hset(cacheKey, cacheField, count, Const.CACHE_EXPIRE_TIME);
			}
			
		} catch (SQLException e) {
			throw new DBException(e);
		} finally {
			ConnectionsAccess.detach(db.name());
			Util.closeQuietly(state);
			Util.closeQuietly(con);
		}
		return count;
	}

	private List<Map<String, Object>> resultsMap(Class<Map<String, Object>> clazz) {

		List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
		Connection con = null;
		PreparedStatement state = null;

		try {
			if (sql == null) {
				sql = dialect.getSelectSql(this, clazz);
			}

			Connection localCon;
			if (transaction == null) {
				localCon = db.getConnection();
				con = localCon; // con gets closed below if non-null
			} else {
				localCon = transaction.getConnection();
			}

			state = localCon.prepareStatement(sql);
			loadArgs(state);

			ResultSet rs = state.executeQuery();

			ResultSetMetaData meta = rs.getMetaData();
			int colCount = meta.getColumnCount();
			
			while (rs.next()) {
				Map<String, Object> map = clazz.newInstance();
				for (int i = 1; i <= colCount; i++) {
					String colName = meta.getColumnName(i);
					map.put(colName, rs.getObject(i));
				}
				out.add(map);
			}
		} catch (InstantiationException e) {
			throw new DBException(e);
		} catch (IllegalAccessException e) {
			throw new DBException(e);
		} catch (SQLException e) {
			throw new DBException(e);
		} catch (IllegalArgumentException e) {
			throw new DBException(e);
		} finally {
			ConnectionsAccess.detach(db.name());
			Util.closeQuietly(state);
			Util.closeQuietly(con);
		}
		return out;
	}

	public <T> Pager<T> page(int page, int limit, Class<T> clazz) {
		
		// query count
		long total = this.count(clazz);
		Pager<T> pager = new Pager<T>(total, page, limit);
		
		String sql = dialect.getPageSql(this, clazz);
		
		int offset = (pager.getPageNum() - 1) * limit;
		
		this.argList.add(offset);
		this.argList.add(limit);
		
		List<T> result = this.list(sql, clazz);
		pager.setList(result);
		return pager;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Serializable> List<T> pklist(Class<?> clazz){
		
		List<T> out = new ArrayList<T>();
		Connection con = null;
		PreparedStatement state = null;
		
		try {
			
			/**
			 * if enabled cache
			 */
			if(this.isCache(clazz)){
				if(null == cacheKey){
					cacheKey = dialect.getModelInfo(clazz).getTable() + "_list";
				}
				cacheKey = dialect.getModelInfo(clazz).getTable() + "_list";
				if(null == cacheField){
					cacheField = getCacheField();
				}
				Object value = db.cache.hget(cacheKey, cacheField);
				if(null != value){
					LOGGER.debug("缓存命中：key = [{}], field = [{}]", cacheKey, cacheField);
					return (List<T>) value;
				}
			}
			
			String querySql = dialect.getPKSql(this, clazz);
			
			Connection localCon;
			if (transaction == null) {
				localCon = db.getConnection();
				con = localCon; // con gets closed below if non-null
			} else {
				localCon = transaction.getConnection();
			}

			LOGGER.debug("Preparing\t=> {}", querySql);

			state = localCon.prepareStatement(querySql);

			// load args
			if (null != argList && !argList.isEmpty()) {
				Object[] args = argList.toArray();
				int marks = Util.getSqlMarks(querySql);
				for (int i = 0; i < marks; i++) {
					state.setObject(i + 1, args[i]);
				}
				LOGGER.debug("Parameters\t=> {}", Arrays.toString(args));
			}
			
			ResultSet rs = state.executeQuery();
			while (rs.next()) {
				Object colValue = rs.getObject(1);
				out.add((T) colValue);
			}
			
			LOGGER.debug("Total\t<= {}", out.size());
			
			if(this.isCache(clazz)){
				LOGGER.debug("缓存设置：key = [{}], field = [{}]", cacheKey, cacheField);
				db.cache.hset(cacheKey, cacheField, out, Const.CACHE_EXPIRE_TIME);
			}
		} catch (SQLException e) {
			throw new DBException(e);
		} finally {
			ConnectionsAccess.detach(db.name());
			Util.closeQuietly(state);
			Util.closeQuietly(con);
		}
		return out;
	}
	
	public <T> List<T> list(Class<T> clazz) {
		return this.list(null, clazz);
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> list(String sql, Class<T> clazz) {
		
		if (Map.class.isAssignableFrom(clazz)) {
			return (List<T>) resultsMap((Class<Map<String, Object>>) clazz);
		}
		
		List<T> out = new ArrayList<T>();
		Connection con = null;
		PreparedStatement state = null;
		
		try {
			
			/**
			 * if enabled cache
			 */
			if(this.isCache(clazz)){
				List<Serializable> pks = this.pklist(clazz);
				if(null != pks){
					for(Serializable pk : pks){
						out.add(db.findByPK(pk, clazz));
					}
				}
				return out;
			}
			
			String querySql =  null != sql ? sql : dialect.getSelectSql(this, clazz);
			
			Connection localCon;
			if (transaction == null) {
				localCon = db.getConnection();
				con = localCon; // con gets closed below if non-null
			} else {
				localCon = transaction.getConnection();
			}
			
			LOGGER.debug("Preparing\t=> {}", querySql);

			state = localCon.prepareStatement(querySql);

			// load args
			Object[] args = this.loadArgs(state);
			
			if(null != args){
				LOGGER.debug("Parameters\t=> {}", Arrays.toString(args));
			}

			ResultSet rs = state.executeQuery();

			ResultSetMetaData meta = rs.getMetaData();
			int colCount = meta.getColumnCount();

			if (Util.isPrimitiveOrString(clazz)) {
				// if the receiver class is a primitive just grab the first
				// column and assign it
				while (rs.next()) {
					Object colValue = rs.getObject(1);
					out.add((T) colValue);
				}
			} else {
				ModelInfo modelInfo = dialect.getModelInfo(clazz);
				while (rs.next()) {
					T row = clazz.newInstance();
					for (int i = 1; i <= colCount; i++) {
						String colName = meta.getColumnName(i);
						Object colValue = rs.getObject(i);
						modelInfo.putValue(row, colName, colValue);
					}
					out.add((T) row);
				}
			}
			
			LOGGER.debug("Total\t<= {}", out.size());
			
		} catch (InstantiationException e) {
			throw new DBException(e);
		} catch (IllegalAccessException e) {
			throw new DBException(e);
		} catch (SQLException e) {
			throw new DBException(e);
		} finally {
			ConnectionsAccess.detach(db.name());
			Util.closeQuietly(state);
			Util.closeQuietly(con);
		}
		return out;
	}

	private Object[] loadArgs(PreparedStatement state) throws SQLException {
		if (null != argList && !argList.isEmpty()) {
			Object[] argsObj = argList.toArray();
			for (int i = 0, len = argsObj.length; i < len; i++) {
				state.setObject(i + 1, argsObj[i]);
			}
			return argsObj;
		}
		return null;
	}
	
	public <T> T insert(Object row) {
		return insert(row, true);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T insert(Object row, boolean commit) {
		insertRow = row;
		sql = dialect.getInsertSql(this, row);
		argList = Arrays.asList(dialect.getInsertArgs(this, row));
		this.execute(commit);
		
		Class<?> clazz = row.getClass();
		if(isCache(clazz)){
			ModelInfo modelInfo = dialect.getModelInfo(clazz);
			String cacheField = modelInfo.getPK(row).toString();
			
			db.cache.del(modelInfo.getTable() + "_list");
			db.cache.del(modelInfo.getTable() + "_count");
			LOGGER.debug("缓存更新：table = [{}], field = [{}]", modelInfo.getTable(), cacheField);
		}
		return (T) insertRow;
	}
	
	public Query update(Object row) {
		return this.update(row, true);
	}
	
	public Query update(Object row, boolean commit) {
		sql = dialect.getUpdateByPKSql(this, row);
		argList = Arrays.asList(dialect.getUpdateArgs(this, row));
		this.execute(commit);
		
		Class<?> clazz = row.getClass();
		if(isCache(clazz)){
			ModelInfo modelInfo = dialect.getModelInfo(clazz);
			
			String cacheKey = modelInfo.getTable() + "_rows";
			String cacheField = modelInfo.getPK(row).toString();
			
			db.cache.del(modelInfo.getTable() + "_count");
			db.cache.hdel(modelInfo.getTable() + "_rows", modelInfo.getPK(row).toString());
			LOGGER.debug("缓存更新：key = [{}], field = [{}]", cacheKey, cacheField);
		}
		return this;
	}
	
	public Query upsert(Object row) {
		return upsert(row, true);
	}
	
	public Query upsert(Object row, boolean commit) {
		sql = dialect.getUpsertSql(this, row);
		argList = Arrays.asList(dialect.getUpsertArgs(this, row));
		this.execute(commit);
		return this;
	}
	
	public Query execute() {
		return this.execute(true);
	}
	
	public Query execute(boolean commit) {
		Connection con = null;
		PreparedStatement state = null;
		try {
			if (transaction == null) {
				con = db.getConnection();
			} else {
				con = transaction.getConnection();
			}
			
			String lowerSql = sql.toLowerCase();
			if (lowerSql.contains("insert")) {
				state = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			} else {
				state = con.prepareStatement(sql);
			}

			LOGGER.debug("Preparing\t=> {}", sql);

			Object[] argsObj = null;
			if (null != argList && !argList.isEmpty()) {
				argsObj = argList.toArray();
				for (int i = 0; i < argsObj.length; i++) {
					state.setObject(i + 1, argsObj[i]);
				}
				LOGGER.debug("Parameters\t=> {}", Arrays.toString(argsObj));
			}
			
			rowsAffected = state.executeUpdate();
			
			if (insertRow != null) {
				ResultSet generatedKeys = state.getGeneratedKeys();
				if (generatedKeys.next()) {
					dialect.populateGeneratedKey(generatedKeys, insertRow);
				}
			}
		} catch (SQLException e) {
			throw new DBException(e);
		} catch (IllegalArgumentException e) {
			throw new DBException(e);
		} finally {
			if (commit) {
				ConnectionsAccess.detach(db.name());
				Util.closeQuietly(state);
				Util.closeQuietly(con);
			}
		}
		return this;
	}
	
	private String getCacheField(){
		if (null != argList && !argList.isEmpty()) {
			StringBuffer sbuf = new StringBuffer();
			Object[] argsObj = argList.toArray();
			for (int i = 0, len = argsObj.length; i < len; i++) {
				sbuf.append(argsObj[i].toString());
			}
			return sbuf.toString();
		}
		return "";
	}
	
	public Query createTable(Class<?> clazz) {
		sql = dialect.getCreateTableSql(clazz);
		this.execute(true);
		return this;
	}

	public Query delete(Object row) {
		return delete(row, true);
	}
	
	public Query delete(Object row, boolean commit) {
		sql = dialect.getDeleteSql(this, row);
		argList = Arrays.asList(dialect.getDeleteArgs(this, row));
		this.execute(commit);
		
		Class<?> clazz = row.getClass();
		if(isCache(clazz)){
			ModelInfo modelInfo = dialect.getModelInfo(clazz);
			String cacheKey = modelInfo.getTable() + "_rows";
			String cacheField = modelInfo.getPK(row).toString();
			db.cache.hdel(cacheKey, modelInfo.getPK(row).toString());
			db.cache.del(modelInfo.getTable() + "_list");
			db.cache.del(modelInfo.getTable() + "_count");
			LOGGER.debug("缓存更新：key = [{}], field = [{}]", cacheKey, cacheField);
		}
		return this;
	}

	public Query delete() {
		return this.delete(true);
	}
	
	public Query delete(boolean commit) {
		String table = getTable();
		if (table == null) {
			throw new DBException("You must specify a table name with the table() method.");
		}
		sql = "delete from " + table;
		if (where != null && where.length() > 0) {
			sql += " where " + where;
		}
		this.execute(commit);
		
		if(isCache(null)){
			db.cache.clean();
			LOGGER.debug("清空缓存");
		}
		return this;
	}

	public Query table(String table) {
		this.table = table;
		return this;
	}

	public int getRowsAffected() {
		return rowsAffected;
	}

	public Query transaction(Transaction trans) {
		this.transaction = trans;
		return this;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public String getWhere() {
		return where.toString();
	}

	public String getTable() {
		return table;
	}

}
