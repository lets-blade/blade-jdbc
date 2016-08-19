package blade.jdbc.cache;

import blade.jdbc.serialize.DbSerializable;
import blade.jdbc.serialize.DefaultDbSerializable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisCache implements Cache {

	private JedisPool pool;
	
	private DbSerializable ot = new DefaultDbSerializable();
	
	public RedisCache(String ip) {
		this.poolInit(ip, 6379);
	}
	
	public RedisCache(String ip, int port) {
		this.poolInit(ip, port);
	}
	
	private synchronized void poolInit(String ip, int port){
		// 建立连接池配置参数
        JedisPoolConfig config = new JedisPoolConfig();
        // 设置最大阻塞时间，毫秒数milliseconds
        config.setMaxWaitMillis(1000);
        // 设置空间连接
        config.setMaxIdle(10);
        // 创建连接池
        pool = new JedisPool(config, ip, 6379);
	}
	
	public Jedis getJedis() {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			jedis.connect();
			// jedis.select(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jedis;
	}
	
	@Override
	public void hset(String key, String field, Object value, int expire) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			
			byte[] data = ot.serialize(value).getBytes();
			jedis.hset(key.getBytes(), field.getBytes(), data);
			if (expire > 0) {
				jedis.expire(key, expire);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	@Override
	public Object hget(String key, String field) {
		Jedis jedis = null;
		String result = null;
		try {
			jedis = getJedis();
			String val = jedis.hget(key, field);
			if(null == val){
				return null;
			}
//			byte[] data = val.getBytes();
			return ot.deserialize(val);
		} catch (Exception e) {
			e.printStackTrace();
			result = null;
		} finally {
			if (jedis != null)
				jedis.close();
		}
		return result;
	}

	@Override
	public void set(String key, Object value, int expire) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			byte[] data = ot.serialize(value).getBytes();
			if (expire > 0) {
				jedis.setex(key.getBytes(), expire, data);
			} else {
				jedis.set(key.getBytes(), data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	@Override
	public Object get(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			String val = jedis.get(key);
			if(null == val){
				return null;
			}
//			byte[] data = val.getBytes();
			return ot.deserialize(val);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	@Override
	public void del(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			jedis.del(key.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	@Override
	public void hdel(String key, String field) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			jedis.hdel(key, field);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	@Override
	public void clean() {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			jedis.del("*");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

}
