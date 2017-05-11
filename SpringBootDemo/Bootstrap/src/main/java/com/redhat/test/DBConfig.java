package com.redhat.test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DBConfig {


	@Value("${dbsecret.username:null}")
	private String username;

	@Value("${dbsecret.password:null}")
	private String password;

	@Value("${dbconfig.hostname}")
	private String hostname;

	@Value("${dbconfig.port}")
	private String port;

    public DBConfig() {
    }

	public String getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}

	@PostConstruct
	public void init(){
		System.out.println(">>>>>>>Database config -->>>>"+hostname+":"+port+" creds "+username +":"+password);
	}
	
}