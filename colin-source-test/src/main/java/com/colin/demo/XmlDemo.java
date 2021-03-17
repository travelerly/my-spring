package com.colin.demo;

import com.colin.bean.Person;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author colin
 * @create 2021-03-12 14:04
 */
public class XmlDemo {

	public static void main(String[] args) {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("bean.xml");
		Person pseron = applicationContext.getBean(Person.class);
		System.out.println(pseron);
	}
}
