package com.colin.bean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author colin
 * @create 2021-03-17 14:34
 */
@Component
public class Cat {

	private String name;

	public Cat() {
		System.out.println("Cat...被创建了...");
	}

	public String getName() {
		return name;
	}

	//自动赋值功能
	@Value("${JAVA_HOME}")
	public void setName(String name) {
		System.out.println("Cat setName 正在赋值调用...");
		this.name = name;
	}
}
