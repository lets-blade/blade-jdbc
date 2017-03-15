package com.blade.jdbc;

import org.sql2o.Sql2o;

import javax.sql.DataSource;

public final class Base {

	public static Sql2o sql2o;

	public static Sql2o open(String url, String user, String password) {
		sql2o = new Sql2o(url, user, password);
		return sql2o;
	}
	
	public static Sql2o open(DataSource dataSource) {
		sql2o = new Sql2o(dataSource);
		return sql2o;
	}

}
