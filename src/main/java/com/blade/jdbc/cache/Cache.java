package com.blade.jdbc.cache;

public interface Cache {
	
	void hset(String key, String field, Object value, int expire);
	
	Object hget(String key, String field);
	
	void set(String key, Object value, int expire);
	
	Object get(String key);
	
	void del(String key);
	
	void hdel(String key, String field);
	
	void clean();
}
