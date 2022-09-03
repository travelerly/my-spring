package com.colin.config;

import com.colin.bean.Phone;
import org.springframework.context.annotation.Bean;
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

	/**
	 * @Bean 标注的组件的方法可视为工厂方法
	 * @Bean 标注的组件类似于用一个工厂方法来创建对象
	 * 会有代理
	 */
	/*@Bean
	public Phone phone(){
		return new Phone();
	}*/
}
