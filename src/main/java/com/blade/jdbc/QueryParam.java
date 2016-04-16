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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import blade.kit.Assert;

public class QueryParam {

	private List<Object> argList = null;
	private StringBuffer sqlB = null;
	
	private QueryParam() {
		argList = new ArrayList<Object>();
		sqlB = new StringBuffer();
	}
	
	public static QueryParam me(){
		return new QueryParam();
	}
	
	public QueryParam eq(String name, Object value){
		Assert.notEmpty(name);
		Assert.notNull(value);
		sqlB.append("and ").append(name).append(" = ? ");
		argList.add(value);
		return this;
	}
	
	public QueryParam notEq(String name, Object value){
		Assert.notEmpty(name);
		Assert.notNull(value);
		sqlB.append("and ").append(name).append(" <> ? ");
		argList.add(value);
		return this;
	}
	
	public QueryParam like(String name, Object value){
		Assert.notEmpty(name);
		Assert.notNull(value);
		sqlB.append("and ").append(name).append(" like ? ");
		argList.add(value);
		return this;
	}
	
	public QueryParam notLike(String name, Object value){
		Assert.notEmpty(name);
		Assert.notNull(value);
		sqlB.append("and ").append(name).append(" not like ? ");
		argList.add(value);
		return this;
	}
	
	public QueryParam gt(String name, Object value){
		Assert.notEmpty(name);
		Assert.notNull(value);
		sqlB.append("and ").append(name).append(" > ? ");
		argList.add(value);
		return this;
	}
	
	public QueryParam gtEt(String name, Object value){
		Assert.notEmpty(name);
		Assert.notNull(value);
		sqlB.append("and ").append(name).append(" >= ? ");
		argList.add(value);
		return this;
	}
	
	public QueryParam lt(String name, Object value){
		Assert.notEmpty(name);
		Assert.notNull(value);
		sqlB.append("and ").append(name).append(" < ? ");
		argList.add(value);
		return this;
	}
	
	public QueryParam ltEt(String name, Object value){
		Assert.notEmpty(name);
		Assert.notNull(value);
		sqlB.append("and ").append(name).append(" <= ? ");
		argList.add(value);
		return this;
	}
	
	public QueryParam between(String name, Object val1, Object val2){
		Assert.notEmpty(name);
		Assert.notNull(val1);
		Assert.notNull(val2);
		sqlB.append("and ").append(name).append(" between ? and ? ");
		argList.add(val1);
		argList.add(val2);
		return this;
	}
	
	public QueryParam or(){
		sqlB.append(" or ");
		return this;
	}
	
	public QueryParam in(String name, Object... values){
		Assert.notEmpty(name);
		Assert.notEmpty(values);
		sqlB.append("and ").append(name).append(" in ( ? ) ");
		argList.add(values);
		return this;
	}
	
	public QueryParam notIn(String name, Object... values){
		Assert.notEmpty(name);
		Assert.notEmpty(values);
		sqlB.append("and ").append(name).append(" not in ( ? ) ");
		argList.add(values);
		return this;
	}
	
	public QueryParam orderby(String orderby){
		Assert.notEmpty(orderby);
		sqlB.append("order by " + orderby + " ");
		return this;
	}
	
	/*public QueryParam orderby(String orderby){
		Assert.notEmpty(orderby);
		sqlB.append("order by ? ");
		argList.add(orderby);
		return this;
	}*/
	
	public QueryParam add(String sql){
		Assert.notEmpty(sql);
		sqlB.append(sql);
		return this;
	}
	
	public QueryParam add(String sql, Object...args){
		Assert.notEmpty(sql);
		Assert.notEmpty(args);
		sqlB.append(sql);
		argList.addAll(Arrays.asList(args));
		return this;
	}
	
	public void page(int page, int count){
		sqlB.append("limit ?,? ");
		argList.add(page);
		argList.add(count);
	}
	
	public String asSql(){
		String sqlbString = sqlB.toString();
		if(sqlbString.startsWith("and")){
			sqlbString = sqlbString.replaceFirst("and", "where");
		}
		return sqlbString;
	}
	
	public Object[] args(){
		if(null != argList && argList.size() > 0){
			return argList.toArray();
		}
		return null;
	}
	
}