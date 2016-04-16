package com.blade.jdbc;

import java.util.List;

import com.blade.jdbc.DB;
import com.blade.jdbc.DBJob;

import blade.kit.logging.Logger;
import blade.kit.logging.LoggerFactory;

public class CRUD {

	private static final Logger LOGGER = LoggerFactory.getLogger(CRUD.class);

	public void before() {
		DB.open("com.mysql.jdbc.Driver", "jdbc:mysql://127.0.0.1/test", "root", "root", true);
	}
	
	public void testQuery() {
		User user = new DBJob<User>() {
			@Override
			public User execute() {
				return connection.createQuery("select * from user_t").executeAndFetchFirst(User.class);
			}
		}.call();

		LOGGER.info(user.toString());

		List<User> list = new DBJob<List<User>>() {
			@Override
			public List<User> execute() {
				return connection.createQuery("select * from user_t").executeAndFetch(User.class);
			}
		}.call();

		LOGGER.info(list.toString());
		
	}

	public void testUpdate() {
		int count = new DBJob<Integer>() {
			@Override
			public Integer execute() {
				return connection.createQuery("update user_t set password = :password where id = :id")
						.addParameter("password", "p@s1wd*").addParameter("id", 4).executeUpdate().getResult();

			}
		}.call();

		LOGGER.info("update count = {}", count);
	}

	public void testInsert() {
		Long id =  (Long) new DBJob<Object>() {
			@Override
			public Object execute() {
				return connection.createQuery("insert into user_t(user_name, password, age) values (:user_name, :password, :age)")
						.addParameter("user_name", "test_insert")
						.addParameter("password", "test_insert")
						.addParameter("age", 15).executeUpdate().getKey();
			}
		}.call(true);
		
		LOGGER.info("insert userid = {}", id);
	}

	public void testInsertWithModel() {
		final User user = new User();
		user.setUser_name("model1");
		user.setPassword("pwd1");
		user.setAge(18);
		
		Long id = (Long) new DBJob<Object>() {
			@Override
			public Object execute() {
				return connection.createQuery("INSERT INTO user_t(user_name, password, age) VALUES (:user_name, :password, :age)")
						.bind(user).executeUpdate().getKey();
			}
		}.call(true);
		
		LOGGER.info("insert userid = {}", id);
	}
	
	
	public void testDelete() {
		
		int count = new DBJob<Integer>() {
			@Override
			public Integer execute() {
				return connection.createQuery("delete from user_t where id = :id").addParameter("id", 4).executeUpdate()
						.getResult();
			}
		}.call();

		LOGGER.info("delete count = {}", count);
	}

	public void testTrans() {
		boolean flag = new DBJob<Boolean>() {
			@Override
			public Boolean execute() {
				
				connection.createQuery("update user_t set password = :password where id = :id")
						.addParameter("p@scwrd", "p@s1wd*").addParameter("id", 4).executeUpdate();
				
				connection.createQuery("update user_t set password = :password where id = :id")
				.addParameter("password", "p@s1wd*").addParameter("id", 1).executeUpdate();
				
				return true;
			}
		}.call(true);

		LOGGER.info("trans flag = {}", flag);
	}

}
