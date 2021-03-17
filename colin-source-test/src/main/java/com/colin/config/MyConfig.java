package com.colin.config;

import com.colin.bean.Cat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author colin
 * @create 2021-03-17 11:31
 */
@Configuration
public class MyConfig {

	@Bean
	public Cat cat(){
		Cat cat = new Cat();
		cat.setName("xiao mao mi");
		return cat;
	}
}
