package blade.jdbc.ds;

import java.util.Properties;

public class JdbcSpec implements ConnectionSpec {

	private final String driver;
    private final String url;
    private final String user;
    private final String password;
    private final Properties properties;
    
	public JdbcSpec(String driver, String url, String user, String password) {
		this.driver = driver;
		this.url = url;
		this.user = user;
		this.password = password;
		this.properties = null;
	}
    
	public JdbcSpec(String driver, String url, Properties properties) {
		this.driver = driver;
		this.url = url;
		this.user = null;
        this.password = null;
		this.properties = properties;
	}
	
	public String getDriver() {
        return driver;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public Properties getProps() {
        return properties;
    }
}
