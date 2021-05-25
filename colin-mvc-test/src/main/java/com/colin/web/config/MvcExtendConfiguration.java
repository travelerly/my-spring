package com.colin.web.config;

import com.colin.web.view.MeiNvResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.util.UrlPathHelper;

import java.util.function.Predicate;

/**
 * @author colin
 * @create 2021-05-05 16:10
 * 目的：自定义视图解析器，和默认的视图解析器都生效
 *
 * WebMvcConfigurer 预留了 MVC 的扩展接口
 */
/*@EnableWebMvc
@Configuration*/
public class MvcExtendConfiguration implements WebMvcConfigurer {

	/*@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		registry.viewResolver(new MeiNvResolver());
		registry.viewResolver(new InternalResourceViewResolver());
	}*/

}
