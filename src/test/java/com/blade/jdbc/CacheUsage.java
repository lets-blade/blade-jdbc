package com.blade.jdbc;

import com.blade.jdbc.AR;
import com.blade.jdbc.DB;
import com.blade.jdbc.cache.memory.FIFOCache;

import blade.kit.logging.Logger;
import blade.kit.logging.LoggerFactory;

public class CacheUsage {

	private static final Logger LOGGER = LoggerFactory.getLogger(CacheUsage.class);
	
	public void before(){
		DB.open("com.mysql.jdbc.Driver", "jdbc:mysql://127.0.0.1/test", "root", "root", true);
		DB.setCache(new FIFOCache());
	}
	
	public void testCache(){
		
		LOGGER.info("第1次请求, 直接查询 >>>> ");
		System.out.println(AR.find("select * from user_t where age > ?", 25).list(User.class));
		
		LOGGER.info("第2次请求, 查询缓存 >>>> ");
		System.out.println(AR.find("select * from user_t where age > ?", 25).list(User.class));
		
		LOGGER.info("第3次请求, 更新数据再查询 >>>> ");
		int r = AR.update("update user_t set password = ? where id = ?", "@null#", 24).executeUpdate();
		System.out.println("r = " + r);
		
		System.out.println(AR.find("select * from user_t where age > ?", 25).list(User.class));
		
		LOGGER.info("第4次请求, 查询缓存 >>>> ");
		System.out.println(AR.find("select * from user_t where age > ?", 25).list(User.class));
		
		LOGGER.info("第5次请求, 删除数据再查询 >>>> ");
		r = AR.update("delete from user_t where id = ?", 27).executeUpdate();
		System.out.println("r = " + r);
		
		System.out.println(AR.find("select * from user_t where age > ?", 25).list(User.class));
		
		LOGGER.info("第6次请求, 查询缓存 >>>> ");
		System.out.println(AR.find("select * from user_t where age > ?", 25).list(User.class));
	}
}
