package com.blade.jdbc.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DefaultCache implements Cache {
	
	/**
	 * 同步缓存容器
	 */
	protected Map<String, CacheObject<String, Object>> _mCache;
	
	/**
	 * 同步缓存容器
	 */
	protected Map<String, Map<String, CacheObject<String, Object>>> _hCache;
	
	/**
	 * 缓存锁
	 */
	protected final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();
	
	/**
	 * 读取锁
	 */
	protected final Lock readLock = cacheLock.readLock();
    
	/**
	 * 写入锁
	 */
	protected final Lock writeLock = cacheLock.writeLock();
	
	/**
	 * 最大缓存数
	 */
	protected int cacheSize;
	
	/**
	 * 默认过期时间, 0 -> 永不过期
	 */
	protected long defaultExpire;
	
	/**
	 * 是否设置默认过期时间
	 */
	protected  boolean existCustomExpire;
	
	public DefaultCache() {
		this(1000);
	}
	
	public DefaultCache(int cacheSize) {
		this.cacheSize	= cacheSize;
		this._mCache	= Collections.synchronizedMap(new HashMap<String, CacheObject<String, Object>>());
		this._hCache	= Collections.synchronizedMap(new HashMap<String, Map<String, CacheObject<String, Object>>>());
	}
	
	@Override
	public void hset(String key, String field, Object value, int expire) {
		writeLock.lock();
		try {
			CacheObject<String, Object> co = new CacheObject<String, Object>(key, value, expire);
			if(expire != 0){
				existCustomExpire = true;
			}
			if(isFull()){
				eliminate() ;
			}
			Map<String, CacheObject<String, Object>> coMap = _hCache.get(key);
			if(null == coMap){
				coMap = new HashMap<String, CacheObject<String, Object>>();
			}
			coMap.put(field, co);
			_hCache.put(key, coMap);
        }
        finally {
            writeLock.unlock();
        }
	}
	
	@Override
	public Object hget(String key, String field) {
		readLock.lock();
        try {
        	Map<String, CacheObject<String, Object>> coMap = _hCache.get(key);
        	
        	if(null == coMap){
        		return null;
        	}
        	
        	CacheObject<String, Object> co = coMap.get(field);
        	
        	if(null == co){
        		return null;
        	}
        	
			if (co.isExpired() == true) {
				coMap.remove(field);
				return null;
			}
			Object value = co.getValue();
			return value;
        } finally {
            readLock.unlock();
        }
	}

	@Override
	public void set(String key, Object value, int expire) {
		writeLock.lock();
		try {
            CacheObject<String, Object> co = new CacheObject<String, Object>(key, value, expire);
            if (expire != 0) {
                existCustomExpire = true;
            }
            if (isFull()) {
                eliminate() ;
            }
            _mCache.put(key, co);
        } finally {
            writeLock.unlock();
        }
	}

	@Override
	public Object get(String key) {
		readLock.lock();
        try {
            CacheObject<String, Object> co = _mCache.get(key);
            if (co == null) {
                return null;
            }
            if (co.isExpired() == true) {
            	_mCache.remove(key);
                return null;
            }
            Object value = co.getValue();
            return value;
        } finally {
            readLock.unlock();
        }
	}

	@Override
	public void del(String key) {
		writeLock.lock();
        try {
            _mCache.remove(key);
        } finally {
            writeLock.unlock();
        }
	}

	@Override
	public void hdel(String key, String field) {
		writeLock.lock();
        try {
        	_hCache.remove(key);
        } finally {
            writeLock.unlock();
        }
	}

	@Override
	public void clean() {
		
	}
	
	public boolean isFull() {
		if (cacheSize == 0) {// o -> 无限制
			return false;
		}
		return ( _mCache.size() + _hCache.size() ) >= cacheSize;
	}
	
	public final int eliminate() {
		writeLock.lock();
        try {
            return eliminateCache();
        }
        finally {
            writeLock.unlock();
        }
	}
	
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
