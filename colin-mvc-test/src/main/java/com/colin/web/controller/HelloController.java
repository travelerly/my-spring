package com.colin.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author colin
 * @create 2021-04-26 15:52
 */
@RestController
public class HelloController {

	@GetMapping("/hello")
	public String hello(){
		return "hello SpringMVC!";
	}
}
