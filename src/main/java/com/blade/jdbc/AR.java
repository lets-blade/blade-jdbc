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

import java.io.Serializable;

import org.sql2o.Connection;

/**
 * AR 类提供了很多直接操作数据库的静态方法，可直接使用
 *
 * @author	<a href="mailto:biezhi.me@gmail.com" target="_blank">biezhi</a>
 * @since	1.0
 */
public final class AR {

	private AR() {
	}
	
	private static boolean isCache = (null != DB.cache);
	
	public static ARC executeSQL(String sql) {
		Connection connection = DB.sql2o.beginTransaction();
		ARC brc = new ARC(connection, sql.toLowerCase(), isCache);
		return brc;
	}
	
	public static ARC executeSQL(String sql, Object...args) {
		Connection connection = DB.sql2o.beginTransaction();
		ARC brc = new ARC(connection, sql.toLowerCase(), isCache, args);
		return brc;
	}
	
	public static ARC executeSQL(Connection connection, String sql) {
		ARC brc = new ARC(connection, sql.toLowerCase(), isCache);
		return brc;
	}
	
	public static ARC executeSQL(Connection connection, String sql, Object...args) {
		ARC brc = new ARC(connection, sql.toLowerCase(), isCache, args);
		return brc;
	}
	
	public static ARC find(String sql) {
		return executeSQL(DB.sql2o.open(), sql);
	}
	
	public static ARC find(String sql, Object ... args) {
		return executeSQL(DB.sql2o.open(), sql, args);
	}
	
	public static ARC find(QueryParam queryParam) {
		return executeSQL(DB.sql2o.open(), queryParam.asSql(), queryParam.args());
	}
	
	public static Object[] in(Object... args) {
		return args;
	}
	
	public static <T extends Serializable> T findById(Class<T> type, Serializable pk) {
		String sql = "select * from " + ARKit.tableName(type) + " where " + ARKit.pkName(type) +" = ?";
		return find(sql, pk).first(type);
	}
	
	public static ARC update(String sql) {
		return executeSQL(DB.sql2o.beginTransaction(), sql);
	}
	
	public static ARC update(String sql, Object ... args) {
		return executeSQL(DB.sql2o.beginTransaction(), sql, args);
	}
	
	/**
	 * 清除数据库缓存
	 */
	public static void cleanCache(){
		if(null != DB.cache){
			DB.cache.clean();
		}
	}
}