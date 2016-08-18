package blade.jdbc.ds;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import blade.jdbc.pool.BasicDataSourceImpl;

public class DataSourceFactory {
	
	static final String DEFAULT_DS_NAME = "blade-jdbc";
	
	public static DataSource createDataSource(String propsPath) throws IOException {
		InputStream in = DataSourceFactory.class.getClassLoader().getResourceAsStream(propsPath);
		Properties props = new Properties();
		props.load(in);
		
		String name = props.getProperty("");
		String driver = props.getProperty("jdbc.driverClassName");
		String url = props.getProperty("jdbc.url");
		String username = props.getProperty("jdbc.username");
		String password = props.getProperty("jdbc.password");
		if(name == null || name.equals("")){
			name = DEFAULT_DS_NAME;
		}
		return new BasicDataSourceImpl(name, driver, url, username, password);
	}
	
}
