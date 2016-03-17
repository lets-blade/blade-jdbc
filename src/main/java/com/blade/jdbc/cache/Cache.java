package com.blade.jdbc.cache;

import java.io.Serializable;

public interface Cache {

	void set(String key, String value);
	
	void set(String key, byte[] value);
	
	void set(String key, Serializable value);
	
	void set(String key, String value, long expire);
	
	void set(String key, byte[] value, long expire);
	
	void set(String key, Serializable value, long expire);
	
	void hset(String key, String field, Serializable value);
	
	void hset(String key, String field, Serializable value, long expire);
	
	void hset(String key, String field, Object value);
	
	void hset(String key, String field, Object value, long expire);
	
	String getString(String key);
	
	byte[] getBytes(String key);
	
	<T extends Serializable> T getObject(String key);
	
	<V> V hget(String key, String field);
	
	boolean del(String key);
	
	boolean hdel(String key);
	
	boolean hdel(String key, String field);
	
}
