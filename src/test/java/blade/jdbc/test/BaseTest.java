package blade.jdbc.test;

import java.io.InputStream;
import java.util.Properties;
import javax.sql.DataSource;
import org.junit.Before;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import blade.jdbc.Base;
import blade.jdbc.ds.DataSourceFactory;

public class BaseTest {

	private DataSource testDefaultPool() {
		try {
			return DataSourceFactory.createDataSource("jdbc.properties");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	private DataSource testHikariPool() {
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

	private DataSource testDruidPool() {
		try {
			InputStream in = BaseTest.class.getClassLoader().getResourceAsStream("druid.properties");
			Properties props = new Properties();
			props.load(in);
			return DruidDataSourceFactory.createDataSource(props);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@Before
	public void before() {
		Base.open(testDefaultPool());
	}

}
