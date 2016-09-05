package com.blade.jdbc.test;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sql2o.Sql2o;

import com.blade.jdbc.Model;
import com.blade.jdbc.Paginator;
import com.blade.jdbc.model.Person;

public class CRUDTest {
	
	Sql2o sql2o = null;
	
	@Before
	public void before(){
		sql2o = new Sql2o("jdbc:mysql://localhost:3306/demo", "root", "root");
		Model.sql2o = sql2o;
	}
	
	@Test
	public void testInsert(){
		Person p = new Person();
		p.set("name", "王爵nice");
		p.set("last_name", "good");
		p.set("dob", "1935-12-06");
		p.set("created_at", new Date());
		p.save();
	}
	
	@Test
	public void testInsertBatch(){
		Person p = new Person();
		for(int i=1; i<=10; i++){
			p.set("name", "batch_" + i);
			p.set("last_name", "good");
			p.set("dob", "1935-12-06");
			p.set("created_at", new Date());
			p.addToBatch();
		}
		p.saveBatch();
	}
	
	@Test
	public void testUpdate(){
		Person p = new Person();
		p.set("name", "王爵nice2").where("id", 108);
		p.update();
	}
	
	@Test
	public void testDelete(){
		Person p = new Person();
		p.where("id", 108);
		p.delete();
	}
	
	@Test
	public void testQuery1(){
		Person p = new Person();
		List<Person> persons = p.all();
		System.out.println(persons);
		
	}
	
	@Test
	public void testQuery2(){
		Person p = new Person();
		List<Person> persons = p.where("id", 108).list();
		System.out.println(persons);
	}
	
	@Test
	public void testQuery3(){
		Person p = new Person();
		Person person = p.findById(108);
		System.out.println(person);
	}
	
	@Test
	public void testQuery4(){
		Person p = new Person();
		int count = p.count();
		System.out.println(count);
	}
	
	@Test
	public void testQuery5(){
		Person p = new Person();
		Paginator<Person> paginator = p.page(1, 10);
		System.out.println(paginator.getList());
	}
	
	@Test
	public void testQuery6(){
		Person p = new Person();
//		List<Person> persons = p.where("name like ?", "%Na%").where("id > ?", 80).order("id desc").list();
		List<Person> persons = p.where("name like ? and id > ? order by ?", "%Na%", 80, "id desc").list();
		System.out.println(persons);
	}
	
	
}
