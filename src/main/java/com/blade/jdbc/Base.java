package com.blade.jdbc;

import org.sql2o.Sql2o;

import javax.sql.DataSource;

public final class Base {

	public static Sql2o open(String url, String user, String password) {
		return new Sql2o(url, user, password);
	}
	
	public static Sql2o open(DataSource dataSource) {
		return new Sql2o(dataSource);
	}

}
