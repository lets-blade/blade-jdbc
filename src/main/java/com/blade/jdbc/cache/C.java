package com.blade.jdbc.cache;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.blade.jdbc.cache.memory.FIFOCache;

public class C {

	public static final int TIME_HOUR = 60 * 60;
	public static final int TIME_DAY = TIME_HOUR * 24;
	private static final int MAX_SIZE = 1000 * 1000 * 50; // 50 mb
	private static final int MAX_COUNT = Integer.MAX_VALUE; // 不限制存放数据的数量
	private static Map<String, Cache> mInstanceMap = new HashMap<String, Cache>();
	
	public static Cache get() {
		return new FIFOCache();
	}
	
	public static Cache get(String cacheDir) {
		return get(cacheDir, "BladeCache");
	}
	
	public static Cache get(String cacheDir, String cacheName) {
		File f = new File(cacheDir, cacheName);
		return get(f, MAX_SIZE, MAX_COUNT);
	}

	public static Cache get(File cacheDir) {
		return get(cacheDir, MAX_SIZE, MAX_COUNT);
	}
	
	public static Cache get(String cacheDir, long max_zise, int max_count) {
		File f = new File(cacheDir, "BladeCache");
		return get(f, max_zise, max_count);
	}

	public static Cache get(File cacheDir, long max_zise, int max_count) {
		Cache manager = mInstanceMap.get(cacheDir.getAbsoluteFile() + myPid());
		if (manager == null) {
			manager = new DiskCache(cacheDir, max_zise, max_count);
			mInstanceMap.put(cacheDir.getAbsolutePath() + myPid(), manager);
		}
		return manager;
	}
	
	private static String myPid() {
		return "_" + System.currentTimeMillis();
	}
	
}
