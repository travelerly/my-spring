package com.colin.spring;

import com.colin.bean.Person;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author colin
 * @create 2021-03-12 14:04
 */
public class MainDemo {

	public static void main(String[] args) {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("bean.xml");
		Person pseron = applicationContext.getBean(Person.class);
		System.out.println("======"+pseron);
	}
}
