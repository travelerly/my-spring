package com.colin.web.service;

import org.springframework.stereotype.Service;

/**
 * @author colin
 * @create 2021-05-02 15:20
 * 这个组件会被 Spring 容器扫描到
 */
@Service
public class HelloService {

	public HelloService() {
 		System.out.println("HelloService 创建对象");
	}

	public String sayHello(String name){
		return "Hello"+name;
	}
}
