package blade.jdbc;

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

import blade.jdbc.dialect.Dialect;
import blade.jdbc.dialect.ModelInfo;
import blade.jdbc.exception.DBException;
import blade.jdbc.tx.Transaction;

public class Query {

	private static final Logger LOGGER = LoggerFactory.getLogger(Query.class);

	private Object insertRow;

	private String sql;
	private String table;
	private StringBuffer where = new StringBuffer();
	private String orderBy;

	private List<Object> argList = new ArrayList<Object>();

	private int rowsAffected;

	private DataBase db;
	private Dialect dialect;
	private Transaction transaction;

	public Query(DataBase db) {
		this.db = db;
		this.dialect = db.getDialect();
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
		this.where(key + " = ?", value);
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
		this.argList = Arrays.asList(args);
		return this;
	}

	public Query sql(String sql, List<Object> argList) {
		this.sql = sql;
		this.argList = argList;
		return this;
	}

	public Query orderBy(String orderBy) {
		this.orderBy = orderBy;
		return this;
	}

	public <T> T first(Class<T> clazz) {
		List<T> list = this.list(clazz);
		if (list.size() > 0) {
			return (T) list.get(0);
		} else {
			return null;
		}
	}
	
	public <T> T findByPK(Serializable pk, Class<T> clazz) {
		ModelInfo modelInfo = dialect.getModelInfo(clazz);
		String pkField = modelInfo.getPrimaryKeyName();
		this.eq(pkField, pk);
		return this.first(clazz);
	}
	
	public <T> Long count(Class<T> clazz){
		
		Long count = 0L;
		
		Connection con = null;
		PreparedStatement state = null;
		
		String querySql = this.sql;
		
		try {
			if (querySql == null) {
				querySql = dialect.getCountSql(this, clazz);
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
			LOGGER.debug("Records\t<= {}", count);
		} catch (SQLException e) {
			throw new DBException(e);
		} finally {
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
			Util.closeQuietly(state);
			Util.closeQuietly(con);
		}
		return out;
	}

	public <T> Pager<T> page(int page, int limit, Class<T> clazz) {
		
		// query count
		Long total = this.count(clazz);
		
		Pager<T> pager = new Pager<T>(total, page, limit);
		
		if (sql == null) {
			sql = dialect.getPageSql(this, clazz);
		}
		
		int offset = (pager.getPageNum() - 1) * limit;
		
		this.argList.add(offset);
		this.argList.add(limit);
		
		List<T> result = this.list(clazz);
		pager.setList(result);
		return pager;
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> list(Class<T> clazz) {

		if (Map.class.isAssignableFrom(clazz)) {
			return (List<T>) resultsMap((Class<Map<String, Object>>) clazz);
		}
		
		List<T> out = new ArrayList<T>();
		Connection con = null;
		PreparedStatement state = null;
		
		String querySql = this.sql;
		try {
			if (querySql == null) {
				querySql = dialect.getSelectSql(this, clazz);
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

			LOGGER.debug("Parameters\t=> {}", Arrays.toString(args));

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
				ModelInfo pojoInfo = dialect.getModelInfo(clazz);
				while (rs.next()) {
					T row = clazz.newInstance();

					for (int i = 1; i <= colCount; i++) {
						String colName = meta.getColumnName(i);
						Object colValue = rs.getObject(i);

						pojoInfo.putValue(row, colName, colValue);
					}
					out.add((T) row);
				}
				LOGGER.debug("Records\t<= {}", out.size());
			}

		} catch (InstantiationException e) {
			throw new DBException(e);
		} catch (IllegalAccessException e) {
			throw new DBException(e);
		} catch (SQLException e) {
			throw new DBException(e);
		} finally {
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
	
	public Query insert(Object row) {
		insertRow = row;
		sql = dialect.getInsertSql(this, row);
		argList = Arrays.asList(dialect.getInsertArgs(this, row));
		this.execute();
		return this;
	}

	public Query upsert(Object row) {
		insertRow = row;
		sql = dialect.getUpsertSql(this, row);
		argList = Arrays.asList(dialect.getUpsertArgs(this, row));
		this.execute();
		return this;
	}

	public Query update(Object row) {
		sql = dialect.getUpdateSql(this, row);
		argList = Arrays.asList(dialect.getUpdateArgs(this, row));
		this.execute();
		return this;
	}

	public Query execute() {

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

			// Set auto generated primary key. The code assumes that the primary
			// key is the only auto generated key.
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
		} /*
			 * finally { Util.closeQuietly(state); Util.closeQuietly(con); }
			 */
		return this;
	}

	public Query createTable(Class<?> clazz) {
		sql = dialect.getCreateTableSql(clazz);
		this.execute();
		return this;
	}

	public Query delete(Object row) {
		sql = dialect.getDeleteSql(this, row);
		argList = Arrays.asList(dialect.getDeleteArgs(this, row));
		this.execute();
		return this;
	}

	public Query delete() {
		String table = getTable();
		if (table == null) {
			throw new DBException("You must specify a table name with the table() method.");
		}
		sql = "delete from " + table;
		if (where != null && where.length() > 0) {
			sql += " where " + where;
		}
		this.execute();
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
