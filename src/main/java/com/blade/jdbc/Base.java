package com.blade.jdbc;

import javax.sql.DataSource;

import org.sql2o.Sql2o;

public final class Base {
	
	public static Sql2o sql2o;
	
	public static void open(String url, String user, String password) {
		sql2o = new Sql2o(url, user, password);
	}
	
	public static void open(DataSource dataSource) {
		sql2o = new Sql2o(dataSource);
	}
	
	public static void execute(String sql){
		sql2o.open().createQuery(sql).executeUpdate();
	}
	
}
