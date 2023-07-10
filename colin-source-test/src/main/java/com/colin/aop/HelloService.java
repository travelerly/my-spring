package com.colin.aop;

import org.springframework.stereotype.Component;

/**
 * @author colin
 * @create 2021-03-23 16:51
 *
 * 切面存在的话就会返回代理对象
 */
//@Component
public class HelloService {

	public HelloService(){
		System.out.println("...HelloService创建了...");
	}

	/**
	 * 切面目标方法
 	 */
	public String sayHello(String name){
		String result = "目标方法执行：你好，"+name;
		System.out.println(result);

		/*// 模拟异常
		Object o1 = new ArrayList<>(10).get(11);*/

		return "你好，返回通知";
	}
}