package com.blade.jdbc.cache;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.blade.jdbc.cache.memory.CacheObject;

/**
 * 
 * <p>
 * 抽象缓存基础实现
 * </p>
 *
 * @author	<a href="mailto:biezhi.me@gmail.com" target="_blank">biezhi</a>
 * @since	1.0
 */
public abstract class MemoryCache implements Cache {
	
	/**
	 * 同步缓存容器
	 */
	protected Map<String, CacheObject<String, Object>> _mCache;
	
	/**
	 * 同步缓存容器
	 */
	protected Map<String, Map<?, CacheObject<String, Object>>> _hCache;
	
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
	protected long defaultExpire = 0;
	
	/**
	 * 是否设置默认过期时间
	 */
	protected  boolean existCustomExpire;

	/**
     * 淘汰对象具体实现
     * @return
     */
    protected abstract int eliminateCache(); 
    
    public MemoryCache() {
    	this(1000);
    }
    
    /**
     * 设置一个缓存大小并初始化
     * @param cacheSize
     */
	public MemoryCache(int cacheSize) {
		this.cacheSize	= cacheSize;
		this._mCache	= Collections.synchronizedMap(new HashMap<String, CacheObject<String, Object>>());
		this._hCache	= Collections.synchronizedMap(new HashMap<String, Map<?, CacheObject<String, Object>>>());
	}
	
	@Override
	public void set(String key, byte[] value) {
		set_(key, value);
	}
	
	@Override
	public void set(String key, Serializable value) {
		set_(key, value);
	}
	
	@Override
	public void set(String key, String value) {
		set_(key, value);
	}
	
	@Override
	public void set(String key, byte[] value, long expire) {
		set_(key, value, expire);
	}
	
	@Override
	public void set(String key, Serializable value, long expire) {
		set_(key, value, expire);
	}
	
	@Override
	public void set(String key, String value, long expire) {
		set_(key, value, expire);
	}
	
	/**
	 * 放一个缓存
	 */
	public void set_(String key, Object obj) {
		set_(key, obj, defaultExpire);
	}

	/**
	 * 放一个缓存并设置缓存时间
	 */
	public void set_(String key, Object value, long expire) {
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
        }
        finally {
            writeLock.unlock();
        }
	}
	
	@Override
	public void hset(String key, String field, Serializable value) {
		this.hset(key, field, value, defaultExpire);
	}
	
	@Override
	public void hset(String key, String field, Serializable value, long expire) {
		this.hset_(key, field, value, expire);
	}
	
	@Override
	public void hset(String key, String field, Object value) {
		this.hset(key, field, value, defaultExpire);
	}
	
	@Override
	public void hset(String key, String field, Object value, long expire) {
		this.hset_(key, field, value, expire);
	}
	
	/**
	 * 放一个缓存
	 */
	public <F> void hset_(String key, F field, Object obj) {
		hset_(key, field, obj, defaultExpire);
	}
	
	/**
	 * 放一个hash类型缓存并设置缓存时间
	 */
	public <F> void hset_(String key, F field, Object value, long expire) {
		writeLock.lock();
		try {
			CacheObject<String, Object> co = new CacheObject<String, Object>(key, value, expire);
			
			if(expire != 0){
				existCustomExpire = true;
			}
			
			if(isFull()){
				eliminate() ;
			}
			
			@SuppressWarnings("unchecked")
			Map<F, CacheObject<String, Object>> coMap = (Map<F, CacheObject<String, Object>>) _hCache.get(key);
			if(null == coMap){
				coMap = new HashMap<F, CacheObject<String, Object>>();
			}
			coMap.put(field, co);
			
			_hCache.put(key, coMap);
        }
        finally {
            writeLock.unlock();
        }
	}
	
	
	/**
	 * 取一个缓存
	 */
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
            return co.getValue();
        } finally {
            readLock.unlock();
        }
	}
	
	@Override
	public String getString(String key) {
		return (String) get(key);
	}
	
	@Override
	public byte[] getBytes(String key) {
		return (byte[]) get(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> T getObject(String key) {
		Object object = get(key);
		if(null != object){
			return (T) object;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <V> V hget(String key, String field) {
		Object object = hget_(key, field);
		if(null != object){
			return (V) object;
		}
		return null;
	}
	
	/**
	 * 取一个缓存
	 */
	public <F> Object hget_(String key, F field) {
		readLock.lock();
        try {
        	Map<?, CacheObject<String, Object>> coMap = _hCache.get(key);
        	
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
			
			return co.getValue();
        } finally {
            readLock.unlock();
        }
	}

	/**
	 * 移除一个缓存
	 */
	public void del(String key) {
		writeLock.lock();
        try {
            _mCache.remove(key);
        } finally {
            writeLock.unlock();
        }
	}
	
	/**
	 * 移除一个缓存
	 */
	public void hdel(String key) {
		writeLock.lock();
        try {
        	_hCache.remove(key);
        } finally {
            writeLock.unlock();
        }
	}
	
	/**
	 * 移除一个缓存
	 */
	public <F> void del(String key, F feild) {
		writeLock.lock();
        try {
        	Map<?, CacheObject<String, Object>> coMap = _hCache.get(key);
        	if(null != coMap){
        		coMap.remove(feild);
        	}
        } finally {
            writeLock.unlock();
        }
	}
	
	public Set<String> keys() {
		return _mCache.keySet();
	}
	
	@SuppressWarnings("unchecked")
	public <F> Set<F> flieds(String key) {
		Map<?, CacheObject<String, Object>> coMap = _hCache.get(key);
		if(null == coMap){
			return null;
		}
		return (Set<F>) coMap.keySet();
	}
	
	public int elementsInCache() {
		return ( _mCache.size() + _hCache.size() );
	}
	
	public int size() {
		return ( _mCache.size() + _hCache.size() );
	}

	protected boolean isNeedClearExpiredObject(){
        return defaultExpire > 0 || existCustomExpire ;
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

	public boolean isFull() {
		if (cacheSize == 0) {// o -> 无限制
			return false;
		}
		return ( _mCache.size() + _hCache.size() ) >= cacheSize;
	}

	public void clear() {
		writeLock.lock();
        try {
        	_mCache.clear();
        } finally {
            writeLock.unlock();
        }
	}

	public int getCacheSize() {
		return cacheSize;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public Cache cacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
		return this;
	}
	
	public Cache expire(long expire) {
		this.defaultExpire = expire;
		return this;
	}
	
}