package com.blade.jdbc;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.sql2o.Sql2o;

public final class Base {

	public static Map<String, Sql2o> sql2oMap = new HashMap<String, Sql2o>(8);

	public static void open(String url, String user, String password) {
		sql2oMap.put(Const.DEFAULT_DB_NAME, new Sql2o(url, user, password));
	}
	
	public static void open(DataSource dataSource) {
		sql2oMap.put(Const.DEFAULT_DB_NAME, new Sql2o(dataSource));
	}
	
	public static void open(String dbName, DataSource dataSource) {
		sql2oMap.put(dbName, new Sql2o(dataSource));
	}

	public static Sql2o database() {
		return sql2oMap.get(Const.DEFAULT_DB_NAME);
	}
	
	public static Sql2o database(String dbName) {
		return sql2oMap.get(dbName);
	}
	
	public static void execute(String sql) {
		database().open().createQuery(sql).executeUpdate();
	}
	
	public static void execute(String dbName, String sql) {
		database(dbName).open().createQuery(sql).executeUpdate();
	}
	
}
