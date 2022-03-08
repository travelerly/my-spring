package com.colin.demo;

import com.colin.bean.Cat;
import com.colin.bean.Person;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author colin
 * @create 2021-03-12 14:04
 */
public class XmlDemo {

	public static void main(String[] args) {
		xmlDemo();
		/*xmlDemo2();*/
	}

	private static void xmlDemo() {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("bean.xml");
		Person person = applicationContext.getBean(Person.class);
		System.out.println("名字：" + person.name);

		/*// 已过时
		XmlBeanFactory xmlBeanFactory = new XmlBeanFactory(new ClassPathResource("bean.xml"));
		Person person = (Person) xmlBeanFactory.getBean("person");
		System.out.println("名字：" + person.name);*/
	}

	private static void xmlDemo2() {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("bean2.xml");
		Cat cat = applicationContext.getBean(Cat.class);
		System.out.println(cat);
	}
}
