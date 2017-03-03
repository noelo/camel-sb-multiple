package com.redhat.test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
//@ConfigurationProperties(prefix = "Secret")
public class DBConfig {

    @Value("${secret.password}")
    private String password;

    @Value("${secret.username}")
    private String username;

    public DBConfig() {
    }

	public String getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}

	@Override
	public String toString() {
		return "DBConfig [password=" + password + ", username=" + username + "]";
	}
	
	
}