package com.colin.web.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author colin
 * @create 2021-05-03 17:52
 */
@RestController
public class TestHelloController {

	@RequestMapping("/test/hello")
	public String testHello(){
		return "Test Hello Controller";
	}
}
