package com.colin.web.controller;


import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author colin
 * @create 2021-05-04 09:00
 *
 * HelloSimpleController 实现了 Controller 接口，支持当前处理器的处理器适配器是 SimpleControllerHandlerAdapter
 */
@org.springframework.stereotype.Controller("/helloSimple")
public class HelloSimpleController implements Controller {

	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return null;
	}
}
