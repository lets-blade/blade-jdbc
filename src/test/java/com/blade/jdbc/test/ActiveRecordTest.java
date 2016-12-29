package com.blade.jdbc.test;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.blade.jdbc.ds.DataSourceFactory;
import com.blade.jdbc.pager.Paginator;
import com.blade.jdbc.ActiveRecord;
import com.blade.jdbc.ar.SampleActiveRecord;
import com.blade.jdbc.persistence.Criteria;
import com.blade.jdbc.test.model.Person;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * Created by biezhi on 2016/12/25.
 */
public class ActiveRecordTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActiveRecordTest.class);

    private ActiveRecord activeRecord;

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

    @Before
    public void before(){
        activeRecord = new SampleActiveRecord(testDefaultPool());
    }

    @Test
    public void testCount(){
        int count = activeRecord.count(new Person());
        LOGGER.info("记录数：{}", count);
    }

    @Test
    public void testList(){
        List<Person> personList = activeRecord.list(new Person());
        LOGGER.info(personList.toString());
    }

    @Test
    public void testCriteriaList(){
        Criteria criteria = new Criteria(Person.class);
        criteria.like("name", "Me%");
        List<Person> persons = activeRecord.list(criteria);
        LOGGER.info(persons.toString());
    }

    @Test
    public void testUpdate(){
        Person temp = new Person();
        temp.setId(1);
        temp.setLast_name("asd");
        activeRecord.update(temp);
    }

    @Test
    public void testPage(){
        Paginator<Person> page = activeRecord.page(new Person(), 2, 10);
        LOGGER.info(page.toString());
    }

}
