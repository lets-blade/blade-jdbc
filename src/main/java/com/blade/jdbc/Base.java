package com.blade.jdbc;

import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blade.jdbc.cache.Cache;

public class Base {

	private static final Logger LOGGER = LoggerFactory.getLogger(Base.class);
	
	private static boolean isLoadDriver = false;
	
	static Cache CACHE;
	
	public static void open(String driver, String url, String user, String password) {
		if (!isLoadDriver) {
			try {
				Class.forName(driver);
			} catch (ClassNotFoundException e) {
				LOGGER.error("driver load error", e);
			}
		}
		new DataBase(Const.DEFAULT_DB_NAME).open(driver, url, user, password);
	}
	
	public static void open(String driver, String url, Properties props) {
		new DataBase(Const.DEFAULT_DB_NAME).open(driver, url, props);
	}
	
	public static DataBase open(DataSource dataSource) {
		DataBase database = new DataBase(Const.DEFAULT_DB_NAME);
		database.open(dataSource);
		Model.db = database;
		return database;
	}
	
	public static void close() {
		new DataBase(Const.DEFAULT_DB_NAME).close();
	}
	
	public static void close(String name) {
		new DataBase(name).close();
	}

}
