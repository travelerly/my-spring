package com.colin.demo;

import com.colin.aop.HelloService;
import com.colin.bean.ContextBean;
import com.colin.bean.Hello;
import com.colin.bean.Person;
import com.colin.config.MyConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author colin
 * @create 2021-03-17 11:34
 */
public class AnnotationDemo {

	public static void main(String[] args) {

		aspectTest();
		/*testFactoryBean();*/
		/*testAutowiredContext();*/
	}

	private static void aspectTest() {
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(MyConfig.class);
		HelloService helloService = applicationContext.getBean(HelloService.class);
		// 代理对象执行目标方法
		helloService.sayHello("小罗");
		System.out.println("==============");
	}

	private static void testFactoryBean() {
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(MyConfig.class);
		Hello hello = applicationContext.getBean(Hello.class);
		Hello hello2 = applicationContext.getBean(Hello.class);
		System.out.println(hello==hello2);
	}

	private static void testAutowiredContext() {
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(MyConfig.class);
		ContextBean contextBean = applicationContext.getBean(ContextBean.class);
		ApplicationContext context = contextBean.getContext();
		System.out.println("注解版Bean中是否可以注入ioc容器==>"+(applicationContext==context));
	}
}
