package com.colin.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author colin
 * @create 2021-03-17 11:31
 */
@EnableAspectJAutoProxy // 开启基于注解的 aop 功能
@ComponentScan("com.colin")
//@ComponentScan("com.colin.bean")
@Configuration
public class MyConfig {

	public MyConfig() {
		System.out.println("...MyConfig创建了...");
	}

	/*@Bean
	public ContextBean contextBean(){
		ContextBean contextBean = new ContextBean();
		contextBean.setName("xiao mao mi");
		return contextBean;
	}

	@Bean
	public Dog dog(){
		Dog dog = new Dog();
		dog.setName("xiao gou");
		return dog;
	}*/
}
