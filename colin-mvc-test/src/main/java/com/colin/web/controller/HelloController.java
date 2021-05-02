package com.colin.web.controller;

import com.colin.web.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author colin
 * @create 2021-04-26 15:52
 */
@Controller
public class HelloController {

	public HelloController() {
		System.out.println("HelloController 创建对象");
	}

	@Autowired
	HelloService helloService;

	@GetMapping("/hello")
	public String hello(){

		String mvc = helloService.sayHello("MVC");

		return mvc;
	}
}
