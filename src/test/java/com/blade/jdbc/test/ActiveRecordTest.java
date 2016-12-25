package com.blade.jdbc.test;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.blade.jdbc.ds.DataSourceFactory;
import com.blade.jdbc.pager.Paginator;
import com.blade.jdbc.ActiveRecord;
import com.blade.jdbc.SampleActiveRecord;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by biezhi on 2016/12/25.
 */
public class ActiveRecordTest {

    protected DataSource testDefaultPool() {
        try {
            return DataSourceFactory.createDataSource("jdbc.properties");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected DataSource testHikariPool() {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(100);
        config.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        config.addDataSourceProperty("serverName", "localhost");
        config.addDataSourceProperty("databaseName", "demo");
        config.addDataSourceProperty("user", "root");
        config.addDataSourceProperty("password", "root");
        config.setInitializationFailFast(true);
        return new HikariDataSource(config);
    }

    protected DataSource testDruidPool() {
        try {
            InputStream in = ActiveRecordTest.class.getClassLoader().getResourceAsStream("druid.properties");
            Properties props = new Properties();
            props.load(in);
            return DruidDataSourceFactory.createDataSource(props);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Test
    public void test1(){

        ActiveRecord activeRecord = new SampleActiveRecord(testDefaultPool());

        int count = activeRecord.count(new Person());
        System.out.println("记录数：" + count);
        System.out.println("end");

//        List<Person> personList = activeRecord.queryList(new Person());
//        System.out.println(personList);

//        Criteria criteria = new Criteria(Person.class);
//        criteria.like("name", "Me%");
//        List<Person> persons = activeRecord.list(criteria);
//        System.out.println(persons);

//        Person temp = new Person();
//        temp.setId(1);
//        temp.setLast_name("asd");
//        activeRecord.update(temp);

        Paginator<Person> page = activeRecord.page(new Person(), 2, 10);
        System.out.println(page);


    }
}
