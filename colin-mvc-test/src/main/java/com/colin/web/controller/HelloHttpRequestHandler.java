package com.colin.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author colin
 * @create 2021-05-04 08:44
 * BeanNameUrlHandlerMapping 创建好对象以后也要初始化，
 * 启动拿到容器中所有的组件，看谁的名字是以"/"开头的，就把这个组件注册为处理器
 *
 * HelloHttpRequestHandler 实现了 HttpRequestHandler 接口，这个处理器对应的适配器是 HttpRequestHandlerAdapter
 */
@Controller("/helloReq") // BeanNameUrlHandlerMapping 就会把他注册为处理器
public class HelloHttpRequestHandler implements HttpRequestHandler {

	// 处理请求
	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().write("===HelloHttpRequestHandler===");
	}
}
