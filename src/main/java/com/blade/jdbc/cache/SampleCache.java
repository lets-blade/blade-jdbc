package com.blade.jdbc.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blade.kit.logging.Logger;
import blade.kit.logging.LoggerFactory;

public class SampleCache implements Cache {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SampleCache.class);
	
	// t_user : asdaclckosii8djlalsd
	private static final Map<String, List<String>> tableKeys = new HashMap<String, List<String>>(128);
	
	// asdaclckosii8djlalsd : object
	private static final Map<String, Object> pool = new HashMap<String, Object>(128);
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(String key) {
		String cahekey = key.split("#")[1];
		Object object = pool.get(cahekey);
		if(null != object){
			return (T) object;
		}
		return null;
	}
	
	@Override
	public <T> void set(String key, T value) {
		String table = key.split("#")[0];
		String cahekey = key.split("#")[1];
		
		List<String> keys = tableKeys.get(table);
		if(null == keys){
			keys = new ArrayList<String>();
		}
		keys.add(cahekey);
		tableKeys.put(table, keys);
		pool.put(cahekey, value);
	}
	
	@Override
	public void clean(String table) {
		List<String> keys = tableKeys.get(table);
		if(null != keys){
			for(String key : keys){
				pool.remove(key);
			}
			LOGGER.debug("==> clean table [{}] cache.", table);
		}
	}

}
