package blade.jdbc;

import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Base {

	private static final Logger LOGGER = LoggerFactory.getLogger(Base.class);
	
	private static boolean isLoadDriver = false;

	public static void open(String driver, String url, String user, String password) {
		if (!isLoadDriver) {
			try {
				Class.forName(driver);
			} catch (ClassNotFoundException e) {
				LOGGER.error("driver load error :", e);
			}
		}
		new DataBase(DataBase.DEFAULT_NAME).open(driver, url, user, password);
	}

	public static void open(String driver, String url, Properties props) {
		new DataBase(DataBase.DEFAULT_NAME).open(driver, url, props);
	}

	public static DataBase open(DataSource dataSource) {
		DataBase database = new DataBase(DataBase.DEFAULT_NAME);
		database.open(dataSource);
		return database;
	}

	public static void close() {
		new DataBase(DataBase.DEFAULT_NAME).close();
	}

	public static void close(String name) {
		new DataBase(name).close();
	}

	// HikariConfig config = new HikariConfig();
	// config.setMaximumPoolSize(100);
	// config.setDataSourceClassName(System.getProperty("norm.dataSourceClassName"));
	// config.addDataSourceProperty("serverName",
	// System.getProperty("norm.serverName"));
	// config.addDataSourceProperty("databaseName",
	// System.getProperty("norm.databaseName"));
	// config.addDataSourceProperty("user", System.getProperty("norm.user"));
	// config.addDataSourceProperty("password",
	// System.getProperty("norm.password"));
	// config.setInitializationFailFast(true);
	// return new HikariDataSource(config);

}
