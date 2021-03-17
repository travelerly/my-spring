package com.colin.bean;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 *
 * Aware接口可以装配Spring底层的组件
 * Bean的功能增强全都是由BeanPostProcessor+InitializingBean(合起来)完成的
 * @author colin
 * @create 2021-03-17 12:04
 */
//@Component
public class Dog implements ApplicationContextAware {

	ApplicationContext context;

	public Dog() {
		System.out.println("dog 创建了");
	}

	String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ApplicationContext getContext() {
		return context;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}
}
