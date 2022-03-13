package com.colin.web;

import com.colin.web.config.MySpringConfig;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

/**
 * @author colin
 * @create 2021-04-26 15:37
 *
 * 只要写了这个类（要实现 WebApplicationInitializer），就相当于配置了 springmvc 的 DispatcherServlet。
 * 1. Tomcat一启动，就会加载这个类「Tomcat 利用 SPI 机制，加载了所有实现了 WebApplicationInitializer 接口的实现类」
 * 		1.创建了容器，指定了配置类，指定了包扫描（所有的 ioc，aop 等 spring 的功能就已经就绪）
 * 		2.注册一个 servlet-->DispatcherServlet
 * 		3.以后的所有请求都交给了 DispatcherServlet
 * 效果：访问 Tomcat 部署的这个 web 应用下的所有请求，都会被 DispatcherServlet 处理
 * DispatcherServlet 就会进入强大的基于注解的 mvc 处理流程（@GetMapping）
 *
 * 必须是 Servlet 3.0 以上才可以，Tomcat 6.0 以上才支持 Servlet 3.0 规范
 * Servlet 3.0 是 javaEE 的 Web 的规范标准，Tomcat 是 Servlet 3.0 规范的一个实现
 *
 * servlet 规范
 * ServletContainerInitializer 这个接口处理 @HandleTypes 注解
 * 这个接口的所有实现类是 Tomcat 使用 SPI 机制加载的
 *
 */
public class AppStarter /*implements WebApplicationInitializer*/ {

	/*@Override*/
	public void onStartup(ServletContext servletContext) throws ServletException {
		// 1.创建了IOC容器
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		// 注册主配置类（注解版的 SpringMVC 配置「替换以前 SpringMVC 的配置文件」）,此时 IOC 容器没有启动刷新
		context.register(MySpringConfig.class);

		// 以上截止，IOC 容器没有启动刷新

		// 2.创建 DispatcherServlet 对象，并保存 IOC 容器。「Spring 会传入 servletContext→{Tomcat} 」
		DispatcherServlet servlet = new DispatcherServlet(context);
		// 利用 servlet 规范添加 servlet
		ServletRegistration.Dynamic registration = servletContext.addServlet("app", servlet);
		registration.setLoadOnStartup(1);
		// 指定好映射路径
		registration.addMapping("/");

		// 3.上面的 DispatcherServlet 添加到 servletContext 里面后，Tomcat 就会对 DispatcherServlet 进行初始化
	}
}
