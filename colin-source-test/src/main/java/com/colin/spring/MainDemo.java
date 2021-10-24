package com.colin.spring;

import com.colin.aop.HelloService;
import com.colin.bean.Person;
import com.colin.config.MyConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author colin
 * @create 2021-03-12 14:04
 */
public class MainDemo {

	public static void main(String[] args) {

		annotationTest();

	}

	private static void annotationTest() {
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(MyConfig.class);

		HelloService helloService = applicationContext.getBean(HelloService.class);
		System.out.println("HelloService = " + helloService);
	}

	private static void xmlTest() {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("bean.xml");
		Person pseron = applicationContext.getBean(Person.class);
		System.out.println("Person = " + pseron);
	}


}
