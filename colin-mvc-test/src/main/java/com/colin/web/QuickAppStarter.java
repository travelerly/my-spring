package com.colin.web;

import com.colin.web.config.MySpringConfig;
import com.colin.web.config.MySpringMVCConfig;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * @author colin
 * @create 2021-05-02 15:07
 * 最快速的整合注解版 SpringMVC 和 Spring
 */
public class QuickAppStarter extends AbstractAnnotationConfigDispatcherServletInitializer {

	/**
	 * 获取根容器的配置（Spring 的配置文件 ===> Spring 的配置类 ===> MySpringConfig.class）
	 */
	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class<?>[]{MySpringConfig.class};
	}

	/**
	 * 获取 Web 容器的配置（SpringMVC 的配置文件 ===> SpringMVC 的配置类 ===> MySpringMVCConfig.class）
	 */
	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class<?>[]{MySpringMVCConfig.class};
	}

	/**
	 * Servlet 的映射,DispatcherServlet 的映射路径
	 */
	@Override
	protected String[] getServletMappings() {
		return new String[]{"/"};
	}
}
