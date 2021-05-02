package com.colin.web;

import com.colin.web.config.SpringConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

/**
 * @author colin
 * @create 2021-04-26 15:37
 *
 * 只要写了这个类，就相当于配置了 springmvc 的 DispatcherServlet
 * 1. Tomcat已启动，就会加载这个类
 * 		1.创建了容器，指定了配置类，制定了包扫描（所有的ioc，aop等spring的功能就已经就绪）
 * 		2.注册一个 servlet-->DispatcherServlet
 * 		3.以后的所有请求都交给了 DispatcherServlet
 * 效果：访问 Tomcat 部署的这个 web 应用下的所有请求，都会被 DispatcherServlet 处理
 * DispatcherServlet 就会进入强大的基于注解的 mvc 处理流程（@GetMapping）
 *
 * 必须是 Servlet 3.0 以上才可以，Tomcat 6.0 以上才支持 Servlet 3.0规范
 * Servlet 3.0 是 javaEE 的 Web 的规范标准，Tomcat 是 Servlet 3.0规范的一个实现
 *
 * servlet规范
 * ServletContainerInitializer这个接口处理@HandleTypes注解
 * 这个接口的所有实现类是Tomcat使用SPI机制加载的
 *
 */
public class AppStarter /*implements WebApplicationInitializer*/ {

	/*@Override*/
	public void onStartup(ServletContext servletContext) throws ServletException {
		// 1.创建了IOC容器
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		// 注册主配置类（注解版的 SpringMVC 配置「替换以前SpringMVC的配置文件」）,此时IOC容器没有启动刷新
		context.register(SpringConfig.class);

		// 2.创建DispatcherServlet对象，并保存IOC容器。「Spring 会传入 servletContext 」
		DispatcherServlet servlet = new DispatcherServlet(context);
		// 利用 servlet 规范添加 servlet
		ServletRegistration.Dynamic registration = servletContext.addServlet("app", servlet);
		registration.setLoadOnStartup(1);
		// 指定好映射路径
		registration.addMapping("/");
		// 3.上面的 DispatcherServlet 添加到 servletContext 里面后，Tomcat 就会对 DispatcherServlet 进行初始化
	}
}
