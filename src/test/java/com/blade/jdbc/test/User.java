package com.blade.jdbc.test;

import java.io.Serializable;

import com.blade.jdbc.annotation.Table;

@Table(value = "user_t", PK = "id")
public class User implements Serializable{

	private static final long serialVersionUID = 7982003725694468659L;
	private Integer id;
	private String user_name;
	private String password;
	private Integer age;

	public User() {
		
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUser_name() {
		return user_name;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", user_name=" + user_name + ", password=" + password + ", age=" + age + "]";
	}

}
