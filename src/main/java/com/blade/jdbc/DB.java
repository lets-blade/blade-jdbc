package com.blade.jdbc;

import javax.sql.DataSource;

import org.sql2o.Sql2o;

import com.blade.jdbc.cache.Cache;
import com.blade.jdbc.ds.BasicDataSourceImpl;

import blade.kit.config.Config;
import blade.kit.config.loader.ConfigLoader;

public class DB {
	
	static Sql2o sql2o;
	
	static Cache cache;
	
	public static void setCache(Cache cache) {
		DB.cache = cache;
	}
	
	public static void open(DataSource dataSource) {
		try {
			sql2o = new Sql2o(dataSource);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void open(String conf) {
		Config config = ConfigLoader.load(conf);
		if(null == conf){
			throw new RuntimeException("load conf error!");
		}
		
		String driver = config.getString("jdbc.driver");
		String url = config.getString("jdbc.url");
		String user = config.getString("jdbc.user");
		String pass = config.getString("jdbc.pass");
		
		open(driver, url, user, pass);
	}
	
	public static void open(String url, String user, String pass) {
		sql2o = new Sql2o(url, user, pass);
	}
	
	public static void open(String driver, String url, String user, String pass){
		open(driver, url, user, pass, true);
	}
	
	public static void open(String driver, String url, String user, String pass, boolean useDs){
		if(useDs){
			BasicDataSourceImpl dataSource = new BasicDataSourceImpl("blade-jdbc-ds", driver, url, user, pass);
			open(dataSource);
		} else {
			try {
				Class.forName(driver);
				open(url, user, pass);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	
}