package com.blade.jdbc.cache.memory;

import java.util.Iterator;

import com.blade.jdbc.cache.MemoryCache;

/**
 * 
 * <p>
 * LFU实现
 * </p>
 *
 * @author	<a href="mailto:biezhi.me@gmail.com" target="_blank">biezhi</a>
 * @since	1.0
 */
public class LFUCache extends MemoryCache {
	
	public LFUCache() {
		this(1000);
	}
	
	public LFUCache(int cacheSize) {
		super(cacheSize);
	}

	/**
	 * 实现删除过期对象 和 删除访问次数最少的对象
	 * 
	 */
	@Override
	protected int eliminateCache() {
		Iterator<CacheObject<String, Object>> iterator = _mCache.values().iterator();
		int count = 0;
		long minAccessCount = Long.MAX_VALUE;
		
		while (iterator.hasNext()) {
			CacheObject<String, Object> cacheObject = iterator.next();

			if (cacheObject.isExpired()) {
				iterator.remove();
				count++;
				continue;
			} else {
				minAccessCount = Math.min(cacheObject.getAccessCount(),
						minAccessCount);
			}
		}

		if (count > 0)
			return count;

		if (minAccessCount != Long.MAX_VALUE) {

			iterator = _mCache.values().iterator();

			while (iterator.hasNext()) {
				CacheObject<String, Object> cacheObject = iterator.next();
				cacheObject.setAccessCount(cacheObject.getAccessCount() - minAccessCount);
				
				if (cacheObject.getAccessCount() <= 0) {
					iterator.remove();
					count++;
				}
			}
		}

		return count;
	}

}