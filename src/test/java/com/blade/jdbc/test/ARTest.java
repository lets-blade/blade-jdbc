package com.blade.jdbc.test;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.blade.jdbc.AR;
import com.blade.jdbc.DB;

public class ARTest {

	@Before
	public void before(){
		DB.open("com.mysql.jdbc.Driver", "jdbc:mysql://127.0.0.1/test", "root", "root", true);
	}
	
	@Test
	public void testQuery(){
		List<User> users = AR.executeSQL("select * from user_t where age > 20 order by id desc limit 0,3").list(User.class);
		System.out.println(users);
		
		List<User> users2 = AR.executeSQL("select * from user_t where age > ? order by ? desc limit ?,?", 20, "id", 0, 3).list(User.class);
		System.out.println(users2);
		
		List<User> users3 = AR.find("where age > 20 order by id desc limit 0,3").list(User.class);
		System.out.println(users3);
		
		User users4 = AR.findById(User.class, 26);
		System.out.println(users4);
	}
	
	@Test
	public void testUpdate(){
		
		int c1 = AR.executeSQL("update user_t set password = 'haha' where id = 26").commit();
		System.out.println(c1);
		
		int c2 = AR.executeSQL("update user_t set password = ? where id = ?", "haha2", 26).commit();
		System.out.println(c2);
		
		int c3 = AR.update("update user_t set password = 'haha3' where id = 26").commit();
		System.out.println(c3);
		
		int c4 = AR.update("update user_t set password = ? where id = ?", "haha4", 26).commit();
		System.out.println(c4);
		
	}
	
}
