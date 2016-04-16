/**
 * Copyright (c) 2016, biezhi 王爵 (biezhi.me@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	
	public static Sql2o sql2o(){
		return sql2o;
	}
	
}