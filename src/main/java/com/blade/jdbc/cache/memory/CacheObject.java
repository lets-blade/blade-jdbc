package com.blade.jdbc.cache.memory;

/**
 * <p>
 * 缓存实体对象
 * </p>
 *
 * @author	<a href="mailto:biezhi.me@gmail.com" target="_blank">biezhi</a>
 * @since	1.0
 */
public class CacheObject<K, V> {

	private K key;
	private V value;
	private long expires; // 对象存活时间(time-to-live)
	private long accessCount;
	private CacheObject<K, V> previous;
	private CacheObject<K, V> next;
	
	/**
	 * @param k
	 * @param v
	 * @param expires	以秒为单位
	 */
	public CacheObject(K k, V v, long expires) {
		this.key = k;
		this.value = v;
		if(expires > 0){
			this.expires = System.currentTimeMillis() + expires*1000;
		} else {
			this.expires = expires;
		}
	}
	
	/**
	 * @return 返回是否已经过期
	 */
	public boolean isExpired() {
		if (expires < 1) {
			return false;
		}
		return expires < System.currentTimeMillis();
	}

	public V getValue() {
		accessCount++;
		return value;
	}

	public K getKey() {
		return key;
	}

	public long getExpires() {
		return expires;
	}

	public void setExpires(long expires) {
		this.expires = expires;
	}
	
	public CacheObject<K, V> getPrevious() {
		return previous;
	}

	public void setPrevious(CacheObject<K, V> previous) {
		this.previous = previous;
	}

	public CacheObject<K, V> getNext() {
		return next;
	}

	public void setNext(CacheObject<K, V> next) {
		this.next = next;
	}

	public void setKey(K key) {
		this.key = key;
	}

	public void setValue(V value) {
		this.value = value;
	}

	public long getAccessCount() {
		return accessCount;
	}

	public void setAccessCount(long accessCount) {
		this.accessCount = accessCount;
	}

}