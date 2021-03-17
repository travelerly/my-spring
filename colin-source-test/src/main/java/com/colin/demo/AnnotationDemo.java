package com.colin.demo;

import com.colin.bean.Cat;
import com.colin.config.MyConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author colin
 * @create 2021-03-17 11:34
 */
public class AnnotationDemo {

	public static void main(String[] args) {
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(MyConfig.class);
		Cat cat = applicationContext.getBean(Cat.class);
		ApplicationContext context = cat.getContext();
		System.out.println("注解版Bean中是否可以注入ioc容器==>"+(applicationContext==context));

	}
}
