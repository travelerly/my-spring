package com.colin.demo;

import com.colin.aop.HelloService;
import com.colin.aop.cycle.AService;
import com.colin.bean.*;
import com.colin.bean.cycle.A;
import com.colin.bean.cycle.B;
import com.colin.config.ConfCglibConfig;
import com.colin.config.MyConfig;
import com.colin.listener.AppEventPublisher;
import com.colin.listener.ChangeEvent;
import com.colin.listener.MessageEvent;
import com.colin.transactional.CglibStudentService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


/**
 * @author colin
 * @create 2021-03-17 11:34
 */
public class AnnotationDemo {

	public static void main(String[] args) {
		/*aopTest();*/
		testBean();
		/*testFactoryBean();*/
		/*testCycle();*/
		/*listenerTest();*/
		/*testCycleAop();*/
		/*testTransactional();*/
		/*testConfCglib();*/

	}

	private static void testConfCglib() {

		/**
		 * 测试"full"配置文件和"lite"配置文件
		 * "full"配置类，其会被 Cglib 所代理
		 * @Configuration："full"配置类，连续两次获取 Car，则 Tank 只创建一次
		 * @Component："lite"配置类，连续两次获取 Car，则 Tank 会创建两次
		 */
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ConfCglibConfig.class);
		Car car_1 = context.getBean(Car.class);
		Car car_2 = context.getBean(Car.class);
	}

	private static void testTransactional() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MyConfig.class);
		// jdk 动态代理
		/*StudentService sutdentService = (StudentService) context.getBean("studentServiceImpl");*/
		// cglib 动态代理
		CglibStudentService sutdentService = (CglibStudentService) context.getBean("cglibStudentService");

		// 用于模拟异常：0-异常
		int exCode = 1;
		sutdentService.saveStudent(1001,"gaolate",exCode);

	}

	/**
	 * 测试事件
	 */
	private static void listenerTest() {
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(MyConfig.class);
		AppEventPublisher appEventPublisher = applicationContext.getBean(AppEventPublisher.class);
		// 测试派发事件
		appEventPublisher.publish(new A());
		appEventPublisher.publish(new MessageEvent("你好 appEventPublisher MessageEvent"));
		appEventPublisher.publish(new ChangeEvent(appEventPublisher,"ending……"));

		applicationContext.publishEvent(new A());
		applicationContext.publishEvent(new MessageEvent("你好 applicationContext MessageEvent"));
		applicationContext.publishEvent(new ChangeEvent(new A(),"ending……"));
	}

	/**
	 * 测试 aop
	 */
	private static void aopTest() {
		/**
		 * @EnableAspectJAutoProxy 开启基于注解的 aop 功能
		 * LogAspect 是普通切面类
		 * CycleAspect 是验证三级缓存解决循环引用的切面类
		 */
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(MyConfig.class);
		HelloService helloService = applicationContext.getBean(HelloService.class);
		// 代理对象执行目标方法
		helloService.sayHello("colin");
		System.out.println("==============");
	}

	/**
	 * 测试普通 bean 及 @Bean 标注的 bean 的创建
	 */
	private static void testBean() {
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(MyConfig.class);
		Hello hello = applicationContext.getBean(Hello.class);
		Hello hello2 = applicationContext.getBean(Hello.class);
		System.out.println(hello==hello2);

		// 测试 @Bean 标注的 bean 的创建
		/*Phone phone = applicationContext.getBean(Phone.class);
		System.out.println("Phone = " + phone);*/
	}

	/**
	 * 测试工厂 bean
	 */
	private static void testFactoryBean(){
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(MyConfig.class);
		HelloFactory helloFactory = applicationContext.getBean(HelloFactory.class);
		Person person = applicationContext.getBean(Person.class);
		/*Hello hello = applicationContext.getBean(Hello.class);
		System.out.println("hello: "+ hello);*/
	}

	/**
	 * 测试 bean 中是否可以注入 ioc 容器
	 */
	private static void testAutowiredContext() {
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(MyConfig.class);
		ContextBean contextBean = applicationContext.getBean(ContextBean.class);
		ApplicationContext context = contextBean.getContext();
		System.out.println("注解版 Bean 中是否可以注入 ioc 容器==>"+(applicationContext==context));
	}

	/**
	 * 测试循环依赖
	 */
	private static void testCycle(){
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(MyConfig.class);
		A a = applicationContext.getBean(A.class);
		B b = applicationContext.getBean(B.class);
	}

	/**
	 * 测试循环依赖的 aop
	 */
	private static void testCycleAop(){
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(MyConfig.class);
		AService aService = applicationContext.getBean(AService.class);
		String result = aService.helloCycleAspect("colin");
		System.out.println(result);
	}
}
