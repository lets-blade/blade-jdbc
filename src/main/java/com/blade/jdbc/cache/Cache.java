package com.blade.jdbc.cache;

public interface Cache {

	<T> T get(String key);
	
	<T> void set(String key, T value);
	
	void clean(String table);
	
	void cleanAll();
	
}