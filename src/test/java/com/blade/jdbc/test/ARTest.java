package com.blade.jdbc.test;

import java.util.List;

import com.blade.jdbc.AR;
import com.blade.jdbc.ARJob;
import com.blade.jdbc.DB;

public class ARTest {

	public static void main(String[] args) {
		
		DB.open("com.mysql.jdbc.Driver", "jdbc:mysql://127.0.0.1/test", "root", "root", true);
		
		List<User> users = AR.find(User.class)
				.where("age > ? and id = ?", 1, 1)
				.orderBy("id asc").list();
		
		System.out.println(users);
		
		User u1 = AR.findById(User.class, 1);
		System.out.println("u1 = " + u1);
		
		// update user set name = ?, age = ? where id = ?
		int count = AR.updateById(User.class, 14)
				.set("user_name", "jaack")
				.set("age", 2)
				.commit();
		
		System.out.println(count);
		
		
		int count2 = AR.delete(User.class)
				.where("id = 14")
				.commit();
		System.out.println(count2);
		
		
		User user = new User();
		user.setAge(22);
		user.setPassword("pas@pcc");
		user.setUser_name("u5878");
		
		int count3 = AR.insertEntity(user).commit();
		System.out.println(count3);
		
		AR.update(User.class);
		
		Integer c = new ARJob<Integer>() {
			@Override
			public Integer execute() {
				
				conn = AR.updateById(User.class, 14)
				.set("user_name", "jaack")
				.set("age", 2).executeUpdate();
				
				AR.updateById(User.class, 14)
				.set("user_name", "jaack")
				.set("age", 2).executeUpdate(conn);
				
				return null;
			}
		}.call();
	}

}
