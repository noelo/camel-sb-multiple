package com.redhat.test;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StubDB {
	
	@Value("${username:notset}")
	private String username;
	
	@Value("${password:notset}")
	private String password;

    @PostConstruct
    public void init(){
        System.out.println(">>>>>>>>>>>>>>>>>>Database config "+username +" "+password);
    }
}