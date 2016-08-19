package blade.jdbc.test;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import blade.jdbc.Base;
import blade.jdbc.Pager;
import blade.jdbc.cache.Cache;
import blade.jdbc.cache.RedisCache;
import blade.jdbc.model.Person;

public class CacheTest extends BaseTest {
	
	private Cache cache;
	
	@Before
	public void before2(){
		// 开启缓存
		cache = new RedisCache("127.0.0.1");
//		cache = new DefaultCache();
		Base.enableCache(cache);
	}
	
	@Test
	public void testC1(){
		cache.set("name", "jack", 3);
		System.out.println(cache.get("name"));
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(cache.get("name"));
	}
	
	/**
	 * 根据主键查询
	 */
	@Test
	public void testByPK(){
		Person person = Person.db.findByPK(1, Person.class);
		person = Person.db.findByPK(1, Person.class);
		person = Person.db.findByPK(1, Person.class);
		person = Person.db.findByPK(1, Person.class);
	}
	
	/**
	 * 查询主键列表
	 */
	@Test
	public void testPKList(){
		List<Integer> ids = Person.db.lt("id", 10).pklist(Person.class);
		ids = Person.db.lt("id", 10).pklist(Person.class);
		ids = Person.db.lt("id", 10).pklist(Person.class);
		ids = Person.db.lt("id", 10).pklist(Person.class);
	}
	
	/**
	 * 查询列表
	 */
	@Test
	public void testSearch(){
		Person.db.lt("id", 50).list(Person.class);
		Person.db.lt("id", 50).list(Person.class);
		Person.db.lt("id", 50).list(Person.class);
		Person.db.lt("id", 50).list(Person.class);
	}
	
	/**
	 * 分页查询
	 */
	@Test
	public void testPage(){
		Pager<Person> person = Person.db.lt("id", 100).page(1, 6, Person.class);
		System.out.println(person);
		Person.db.lt("id", 100).page(1, 6, Person.class);
		Person.db.lt("id", 100).page(1, 6, Person.class);
		Person.db.lt("id", 100).page(1, 6, Person.class);
	}
	
	/**
	 * 查询一条
	 */
	@Test
	public void testFindOne(){
		Person.db.eq("id", 1).first(Person.class);
		Person.db.eq("id", 1).first(Person.class);
		Person.db.eq("id", 1).first(Person.class);
		Person.db.eq("id", 1).first(Person.class);
	}
	
	/**
	 * 查询记录数
	 */
	@Test
	public void testCount(){
		Person.db.like("name", "ja%").count(Person.class);
		Person.db.like("name", "ja%").count(Person.class);
		Person.db.like("name", "ja%").count(Person.class);
		Person.db.like("name", "ja%").count(Person.class);
	}
	
	/**
	 * 测试本次查询关闭缓存
	 */
	@Test
	public void testCloseCache(){
		Person.db.like("name", "ja%").cached(false).count(Person.class);
	}
	
}
