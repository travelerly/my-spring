package com.colin.web.controller;

import com.colin.web.exception.InvalidUserException;
import com.colin.web.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

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

	@RequestMapping("/simple")
	public String testSimpleMethod(){
		return "Hello Simple Method";
	}

	@GetMapping("/sayHello")
	public String sayHello(String name, // 可以从请求参数中获取
						@RequestParam("user") String user, // 可以从请求参数中获取
						HttpSession session ,// 原生的 session 对象
						Integer num
						){
		// 模拟运行时异常
		int a = 10 / num;

		// 模拟自定义异常
		if ("abc".equals(user)){
			// 非法的用户，抛出自定义异常
			throw new InvalidUserException();
		}

		// 方法的签名，详细参照：https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-ann-arguments
		String mvc = helloService.sayHello(user+" MVC "+name);
		session.setAttribute("msg",mvc);
		// 方法返回值的写法，详细参照：https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-ann-return-types
		return "index.jsp";
	}

	@GetMapping("/meinv")
	public String meinv(String name, Model model){

		// 模拟数据库查询数据
		model.addAttribute("info","meinv info");

		// 页面渲染的效果是美女的详情
		return "meinv:"+name;
	}
}
