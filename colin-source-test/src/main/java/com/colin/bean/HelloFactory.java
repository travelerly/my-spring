package com.colin.bean;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

/**
 *
 * 工厂Bean和普通Bean的区别：
 * 1.例如Person是普通Bean，注册的组件对象就是Person对象，类型就是Person.class
 * 2.例如HelloFactory是工厂Bean，实现了FactoryBean接口，注册的组件对象就不是HelloFactory，
 * 		而是HelloFactory这个工厂调用了getObject()方法返回的对象，类型是getObjectType()指定的类型
 * 		Mybatis和Spring的整合就是采用了此种方式，SQLSessionFactory
 *
 * @author colin
 * @create 2021-03-19 08:57
 */
@Component  // 也可以通过实现 SmartFactoryBean 来指定提前加载
public class HelloFactory implements FactoryBean<Hello> {

	@Override
	public Hello getObject() throws Exception {
		// 这是最终获取的对象
		return new Hello();
	}

	@Override
	public Class<?> getObjectType() {
		return Hello.class;
	}
}
