package com.colin.demo;

import com.colin.bean.Dog;
import com.colin.config.MyConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author colin
 * @create 2021-03-17 12:04
 */
public class AwareDemo {

	public static void main(String[] args) {
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(MyConfig.class);
		Dog dog = applicationContext.getBean(Dog.class);
		ApplicationContext context = dog.getContext();
		System.out.println("Aware接口是否可以注入ioc容器==>"+(applicationContext==context));
	}
}
