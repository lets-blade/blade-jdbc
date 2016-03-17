package com.blade.jdbc.cache.memory;

import java.util.Iterator;

import com.blade.jdbc.cache.MemoryCache;

/**
 * 
 * <p>
 * FIFO实现
 * </p>
 *
 * @author	<a href="mailto:biezhi.me@gmail.com" target="_blank">biezhi</a>
 * @since	1.0
 */
public class FIFOCache extends MemoryCache {

	public FIFOCache() {
		super(1000);
	}
	
	public FIFOCache(int cacheSize) {
		super(cacheSize);
	}

	@Override
	protected int eliminateCache() {
		
		int count = 0;
		String firstKey = null;

		Iterator<CacheObject<String, Object>> iterator = _mCache.values().iterator();
		while (iterator.hasNext()) {
			CacheObject<String, Object> cacheObject = iterator.next();

			if (cacheObject.isExpired()) {
				iterator.remove();
				count++;
			} else {
				if (firstKey == null)
					firstKey = cacheObject.getKey();
			}
		}
		
		if (firstKey != null && isFull()) {// 删除过期对象还是满,继续删除链表第一个
			_mCache.remove(firstKey);
		}

		return count;
	}


}