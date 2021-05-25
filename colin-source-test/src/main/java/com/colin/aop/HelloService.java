package com.colin.aop;

import org.springframework.stereotype.Component;

/**
 * @author colin
 * @create 2021-03-23 16:51
 */
@Component  // 切面存在的化就会返回代理对象
public class HelloService {

	public HelloService(){
		System.out.println("...HelloService创建了...");
	}

	// 切面目标方法
	public String sayHello(String name){
		String result = "你好："+name;
		System.out.println(result);
		int length = name.length();
		return result + "---" + length;
	}
}