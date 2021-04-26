package com.colin.web;

import com.colin.web.config.AppConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

/**
 * @author colin
 * @create 2021-04-26 15:37
 *
 * 只要写了这个类，就相当于配置了springmvc的DispatcherServlet
 * 1. Tomcat已启动，就会加载他
 * 		1.创建了容器，指定了配置类（所有的ioc，aop等spring的功能就已经就绪）
 * 		2.注册一个servlet，DispatcherServlet
 * 		3.以后的所有请求都交给了DispatcherServlet
 * 效果：访问Tomcat部署的这个web应用下的所有请求，都有DispatcherServlet处理
 * DispatcherServlet就会进入强大的基于注解的mvc处理流程（@GetMapping）
 *
 * 必须是Servlet 3.0以上才可以，Tomcat 6.0以上才支持Servlet 3.0规范
 * Servlet 3.0是javaEE的Web的规范标准，Tomcat是Servlet 3.0规范的一个实现
 */
@Component
public class AppStarter implements WebApplicationInitializer {
	@Override
	public void onStartup(javax.servlet.ServletContext servletContext) throws ServletException {
		// 创建了IOC容器
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		context.register(AppConfig.class);

		// 配置了DispatcherServlet
		DispatcherServlet servlet = new DispatcherServlet(context);
		ServletRegistration.Dynamic registration = servletContext.addServlet("app", servlet);
		registration.setLoadOnStartup(1);
		registration.addMapping("/");
	}
}
