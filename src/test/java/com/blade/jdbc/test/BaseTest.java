package com.blade.jdbc.test;

import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.junit.Before;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.blade.jdbc.Base;
import com.blade.jdbc.ds.DataSourceFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class BaseTest {
	
	protected DataSource testDefaultPool() {
		try {
			return DataSourceFactory.createDataSource("jdbc.properties");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	protected DataSource testHikariPool() {
		HikariConfig config = new HikariConfig();
		config.setMaximumPoolSize(100);
		config.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
		config.addDataSourceProperty("serverName", "localhost");
		config.addDataSourceProperty("databaseName", "demo");
		config.addDataSourceProperty("user", "root");
		config.addDataSourceProperty("password", "root");
		config.setInitializationFailFast(true);
		return new HikariDataSource(config);
	}

	protected DataSource testDruidPool() {
		try {
			InputStream in = BaseTest.class.getClassLoader().getResourceAsStream("druid.properties");
			Properties props = new Properties();
			props.load(in);
			return DruidDataSourceFactory.createDataSource(props);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	@Before
	public void before() {
//		Base.open("jdbc:mysql://localhost:3306/demo", "root", "root");
		Base.open(testDefaultPool());
	}

}
