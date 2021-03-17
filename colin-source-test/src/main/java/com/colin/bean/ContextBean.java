package com.colin.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author colin
 * @create 2021-03-17 11:32
 */
//@Component
public class ContextBean {

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	//测试注解版Bean中能否注入ioc容器
	@Autowired
	ApplicationContext context;

	public ApplicationContext getContext() {
		return context;
	}
}
