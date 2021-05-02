package com.colin.web;

import com.colin.web.config.SpringConfig;
import com.colin.web.config.SpringMVCConfig;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * @author colin
 * @create 2021-05-02 15:07
 * 最快速的整合注解版 SpringMVC 和 Spring
 */
public class QuickAppStarter extends AbstractAnnotationConfigDispatcherServletInitializer {

	/**
	 * 获取跟容器的配置（Spring 的配置文件===> Spring 的配置类）
	 */
	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class<?>[]{SpringConfig.class};
	}

	/**
	 * 获取 Web 容器的配置（SpringMVC 的配置文件===> SpringMVC 的配置类）
	 */
	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class<?>[]{SpringMVCConfig.class};
	}

	/**
	 * Servlet 的映射,DispatcherServlet 的映射路径
	 */
	@Override
	protected String[] getServletMappings() {
		return new String[]{"/"};
	}
}
