package com.colin.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


/**
 * @author colin
 * @create 2021-03-17 11:31
 */
@ComponentScan("com.colin")
@Configuration
public class MyConfig {

	public MyConfig() {
		System.out.println("...MyConfig创建了...");
	}

}
