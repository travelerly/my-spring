package com.colin.bean;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

/**
 *
 * 工厂 Bean 和普通 Bean 的区别：
 * 1.例如 Person 是普通 Bean，注册的组件对象就是 Person 对象，类型就是 Person.class
 * 2.例如 HelloFactory 是工厂 Bean，实现了 FactoryBean 接口，其主要目的不是注册组件对象 HelloFactory，
 * 		而是使用 HelloFactory 这个工厂调用了 getObject() 方法返回的对象，类型是 getObjectType() 指定的类型
 * 		Mybatis 和 Spring 的整合就是采用了此种方式，SQLSessionFactory
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
