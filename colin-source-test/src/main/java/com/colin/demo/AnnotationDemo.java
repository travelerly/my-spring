package com.colin.demo;

import com.colin.aop.HelloService;
import com.colin.bean.ContextBean;
import com.colin.bean.Hello;
import com.colin.bean.cycle.A;
import com.colin.bean.cycle.B;
import com.colin.bean.cycle.X;
import com.colin.bean.cycle.Y;
import com.colin.config.MyConfig;
import com.colin.listener.AppEventPublisher;
import com.colin.listener.ChangeEvent;
import com.colin.listener.MessageEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


/**
 * @author colin
 * @create 2021-03-17 11:34
 */
public class AnnotationDemo {

	public static void main(String[] args) {
		/*aopTest();*/
		testFactoryBean();
		/*testCycle();*/
		/*listenerTest();*/
	}

	private static void listenerTest() {
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(MyConfig.class);
		AppEventPublisher appEventPublisher = applicationContext.getBean(AppEventPublisher.class);
		// 测试派发事件
		appEventPublisher.publish(new A());
		appEventPublisher.publish(new MessageEvent("你好 MessageEvent"));
		appEventPublisher.publish(new ChangeEvent(appEventPublisher,"ending……"));
	}

	private static void aopTest() {
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(MyConfig.class);
		HelloService helloService = applicationContext.getBean(HelloService.class);
		// 代理对象执行目标方法
		helloService.sayHello("colin");
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

	private static void testCycle(){
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(MyConfig.class);
		X x = applicationContext.getBean(X.class);
		Y y = applicationContext.getBean(Y.class);
		/*A a = applicationContext.getBean(A.class);
		B b = applicationContext.getBean(B.class);*/
	}
}
