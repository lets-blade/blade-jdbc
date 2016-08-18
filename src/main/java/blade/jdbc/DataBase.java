package blade.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blade.jdbc.cache.StatementCache;
import blade.jdbc.dialect.DefaultDialect;
import blade.jdbc.dialect.Dialect;
import blade.jdbc.ds.ConnectionSpec;
import blade.jdbc.ds.DataSourceManager;
import blade.jdbc.ds.JdbcSpec;
import blade.jdbc.exception.DBException;
import blade.jdbc.exception.InitException;
import blade.jdbc.tx.Transaction;

/**
 * Provides methods to access a database.
 */
public class DataBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataBase.class);

	public static final String DEFAULT_NAME = "default_blade_1";
	
	private Dialect dialect = new DefaultDialect();
	private DataSource datasource;
	private String name = DEFAULT_NAME;
	
	public DataBase(String name) {
		this.name = name;
	}
	
	public String name() {
		return this.name;
	}
	
	public void open(String driver, String url, String user, String pass) {
		try {
			Connection connection = DriverManager.getConnection(driver, user, pass);
			ConnectionsAccess.attach(DataBase.DEFAULT_NAME, connection, url);
		} catch (SQLException e) {
			throw new InitException("Failed to connect to JDBC URL: " + url, e);
		}
	}
	
	public void open(String driver, String url, Properties props) {
		checkExistingConnection(name);
		try {
			Class.forName(driver);
			Connection connection = DriverManager.getConnection(url, props);
			ConnectionsAccess.attach(name, connection, url);
		} catch (Exception e) {
			throw new InitException("Failed to connect to JDBC URL: " + url, e);
		}
	}
	
	public void open(DataSource dataSource) {
		checkExistingConnection(name);
		try {
			this.datasource = dataSource;
			Connection connection = datasource.getConnection();
			ConnectionsAccess.attach(name, connection, datasource.toString());
		} catch (SQLException e) {
			throw new InitException(e);
		}
	}

	public void open(ConnectionSpec spec) {
		checkExistingConnection(name);
		if (spec instanceof JdbcSpec) {
			openJdbc((JdbcSpec) spec);
		} else {
			throw new IllegalArgumentException("this spec not supported: " + spec.getClass());
		}
	}

	private void openJdbc(JdbcSpec spec) {
		if (spec.getProps() != null) {
			open(spec.getDriver(), spec.getUrl(), spec.getProps());
		} else {
			open(spec.getDriver(), spec.getUrl(), spec.getUser(), spec.getPassword());
		}
	}

	public void attach(Connection connection) {
		ConnectionsAccess.attach(name, connection, "");
	}

	public void setDialect(Dialect dialect) {
		this.dialect = dialect;
	}

	public Dialect getDialect() {
		return dialect;
	}

	/**
	 * Provides the DataSource used by this database. Override this method to
	 * change how the DataSource is created or configured.
	 */
	public DataSource getDataSource() {
		return this.datasource;
	}

	private void checkExistingConnection(String name) {
		if (null != ConnectionsAccess.getConnection(name)) {
			throw new DBException(
					"Cannot open a new connection because existing connection is still on current thread, name: " + name
							+ ", connection instance: " + getConnection()
							+ ". This might indicate a logical error in your application.");
		}
	}

	/**
	 * Create a query using straight SQL. Overrides any other methods like
	 * .where(), .orderBy(), etc.
	 * 
	 * @param sql
	 *            The SQL string to use, may include ? parameters.
	 * @param args
	 *            The parameter values to use in the query.
	 */
	public Query sql(String sql, Object... args) {
		return new Query(this).sql(sql, args);
	}

	/**
	 * Create a query with the given where clause.
	 * 
	 * @param where
	 *            Example: "name=?"
	 * @param args
	 *            The parameter values to use in the where, example: "Bob"
	 */
	public Query where(String where, Object... values) {
		return new Query(this).where(where, values);
	}

	public Query like(String key, Object value) {
		return new Query(this).like(key, value);
	}
	
	public Query notLike(String key, Object value) {
		return new Query(this).notLike(key, value);
	}

	public Query gt(String key, Object value) {
		return new Query(this).gt(key, value);
	}

	public Query gte(String key, Object value) {
		return new Query(this).gte(key, value);
	}

	public Query lt(String key, Object value) {
		return new Query(this).lt(key, value);
	}

	public Query lte(String key, Object value) {
		return new Query(this).lte(key, value);
	}

	public Query eq(String key, Object value) {
		return new Query(this).eq(key, value);
	}

	public Query neq(String key, Object value) {
		return new Query(this).neq(key, value);
	}

	public Query in(String key, Object[] values) {
		return new Query(this).in(key, values);
	}
	
	public Query notIn(String key, Object[] values) {
		return new Query(this).notIn(key, values);
	}
	
	public Query between(String key, Object value1, Object value2) {
		return new Query(this).between(key, value1, value2);
	}
	
	/**
	 * Create a query with the given "order by" clause.
	 */
	public Query orderBy(String orderBy) {
		return new Query(this).orderBy(orderBy);
	}
	
	/**
	 * Returns a JDBC connection. Can be useful if you need to customize how
	 * transactions work, but you shouldn't normally need to call this method.
	 * You must close the connection after you're done with it.
	 */
	public Connection getConnection() {
		Connection connection = ConnectionsAccess.getConnection(name);
		
		if(null == connection){
			if (datasource == null) {
				datasource = DataSourceManager.getDataSource();
			}
			if(null != datasource){
				try {
					connection = datasource.getConnection();
					ConnectionsAccess.attach(name, connection, null);
				} catch (SQLException e) {
					throw new DBException(e);
				}
			}
		}
		
		if (connection == null) {
			throw new DBException("there is no connection '" + name + "' on this thread, are you sure you opened it?");
		}
		return connection;
	}

	/**
	 * Simple, primitive method for creating a table based on a pojo. Does not
	 * add indexes or implement complex data types. Probably not suitable for
	 * production use.
	 */
	public Query createTable(Class<?> clazz) {
		return new Query(this).createTable(clazz);
	}

	/**
	 * Insert a row into a table. The row pojo can have a @Table annotation to
	 * specify the table, or you can specify the table with the .table() method.
	 */
	public Query insert(Object row) {
		return new Query(this).insert(row);
	}

	/**
	 * Delete a row in a table. This method looks for an @Id annotation to find
	 * the row to delete by primary key, and looks for a @Table annotation to
	 * figure out which table to hit.
	 */
	public Query delete(Object row) {
		return new Query(this).delete(row);
	}

	/**
	 * Execute a "select" query and get some results. The system will create a
	 * new object of type "clazz" for each row in the result set and add it to a
	 * List. It will also try to extract the table name from a @Table annotation
	 * in the clazz.
	 */
	public <T> List<T> list(Class<T> clazz) {
		return new Query(this).list(clazz);
	}
	
	public <T> List<T> page(int page, int count, Class<T> clazz) {
		return new Query(this).page(page, count, clazz);
	}

	/**
	 * Returns the first row in a query in a pojo. Will return it in a Map if a
	 * class that implements Map is specified.
	 */
	public <T> T first(Class<T> clazz) {
		return new Query(this).first(clazz);
	}
	
	public <T> Long count(Class<T> clazz) {
		return new Query(this).count(clazz);
	}

	/**
	 * Update a row in a table. It will match an existing row based on the
	 * primary key.
	 */
	public Query update(Object row) {
		return new Query(this).update(row);
	}
	
	/**
	 * Upsert a row in a table. It will insert, and if that fails, do an update
	 * with a match on a primary key.
	 */
	public Query upsert(Object row) {
		return new Query(this).upsert(row);
	}

	/**
	 * Create a query and specify which table it operates on.
	 */
	public Query table(String table) {
		return new Query(this).table(table);
	}

	/**
	 * Start a database transaction. Pass the transaction object to each query
	 * or command that should be part of the transaction using the
	 * .transaction() method. Then call transaction.commit() or .rollback() to
	 * complete the process. No need to close the transaction.
	 * 
	 * @return a transaction object
	 */
	public Transaction startTransaction() {
		Transaction trans = new Transaction();
		trans.setConnection(getConnection());
		return trans;
	}

	/**
	 * Create a query that uses this transaction object.
	 */
	public Query transaction(Transaction trans) {
		return new Query(this).transaction(trans);
	}
	
	public void transaction(Runnable runnable) {
		Transaction transaction = startTransaction();
		try {
			runnable.run();
		} catch (Exception e) {
			transaction.rollback();
			throw new DBException(e);
		}
		transaction.commit();
		transaction = null;
	}

	public void close() {
		close(false);
	}

	public void close(boolean suppressWarning) {
		try {
			Connection connection = ConnectionsAccess.getConnection(name);
			if (connection == null) {
				throw new DBException("cannot close connection '" + name + "' because it is not available");
			}
			StatementCache.instance().cleanStatementCache(connection);
			connection.close();
			LOGGER.debug("Closed connection: {}", connection);
		} catch (Exception e) {
			if (!suppressWarning) {
				LOGGER.warn("Could not close connection! MUST INVESTIGATE POTENTIAL CONNECTION LEAK!", e);
			}
		} finally {
			ConnectionsAccess.detach(name); // let's free the thread from connection
		}
	}
	
}
