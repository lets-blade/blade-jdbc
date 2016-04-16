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

import org.sql2o.Connection;
import org.sql2o.Sql2oException;

import com.blade.jdbc.exception.CallException;

public abstract class DBJob<T> {
	
	protected Connection connection;
	
	public abstract T execute();
	
	public T call(){
		return call(false);
	}
	
	public synchronized T call(boolean dml){
		try {
			connection = DB.sql2o.beginTransaction();
			T t = execute();
			if(dml){
				connection.commit();
			}
			return t;
		} catch (Sql2oException e) {
			if(dml){
				connection.rollback();
			}
			throw new CallException(e.getMessage());
		}  catch (Exception e) {
			if(dml){
				connection.rollback();
			}
			throw new CallException(e);
		} finally {
			if(null != connection){
				connection.close();
			}
		}
	}
	
}
