package com.blade.jdbc;

import org.sql2o.Connection;
import org.sql2o.Sql2oException;

import com.blade.jdbc.exception.CallException;

public abstract class ARJob<T> {
	
	protected Connection conn;
	
	public abstract T execute();
	
	public synchronized T call(){
		try {
			T t = execute();
			if(null != conn){
				conn.commit();
			}
			return t;
		} catch (Sql2oException e) {
			if(null != conn){
				conn.rollback();
			}
			throw new CallException(e.getMessage());
		}  catch (Exception e) {
			if(null != conn){
				conn.rollback();
			}
			throw new CallException(e);
		} finally {
			if(null != conn){
				conn.close();
			}
		}
	}
	
}
