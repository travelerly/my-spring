package com.colin.web.config;

import com.colin.web.view.MyViewResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * @author colin
 * @create 2021-05-05 16:10
 *
 * 目的：自定义视图解析器，和默认的视图解析器都生效
 * WebMvcConfigurer 预留了 MVC 的扩展接口，可以扩展 MVC 的很多组件，下面以扩展视图解析器为例。
 *
 * SpringMVC 的两种启用方式
 * 1. 使用注解 @EnableWebMvc 开启 SpringMVC 功能，此方式修改了 SpringMVC 底层行为，只需要实现 WebMvcConfigurer 接口，重写方法来配置组件功能。WebMvcConfigurer 预留了 SpringMVC 的扩展接口，可扩展 SpringMVC 的很多功能组件；
 * 2. SpringMVC 默认规则，即所有组件都是在 DispatcherServlet 初始化的时候直接使用配置文件中指定的默认组件。这种方式没有预留扩展接口，如需扩展，则要自己重新替换相应组件；
 *
 * WebMvcConfigurer + @EnableWebMvc 实现了定制和扩展 SpringMVC 的功能
 * @EnableWebMvc 导入的类「DelegatingWebMvcConfiguration.class」会给容器中放入 SpringMVC 的很多核心组件，例如 HandlerMapping，ViewResolver等。
 * 并且这些组件的功能在扩展的时候都是留给接口 WebMvcConfigurer「其实现类属于访问者，拿到真正的内容进行修改」介入并定制的，例如 WebMvcConfigurer 的实现类可以配置自定义视图解析器。
 *
 * DelegatingWebMvcConfiguration 的作用
 * 1. 其父类-WebMvcConfigurationSupport 中含有 @Bean 方法，给容器中放入组件；
 * 2. 每一个组件的核心处都采用了模板方法，留给子类 DelegatingWebMvcConfiguration 来实现；
 * 3. 只要这个 DelegatingWebMvcConfiguration 生效，则从容器中拿到所有的 configurers「WebMvcConfigurer 的实现类」完成相应功能；而启用 DelegatingWebMvcConfiguration 有以下几种方式
 *    1. 任意配置类上加注解 @EnableWebMvc，然后实 WebMvcConfigurer 接口，进行扩展；
 *    2. 任意配置类继承 DelegatingWebMvcConfiguration，然后实现 WebMvcConfigurer 接口，进行扩展；
 *    3. 任意配置类继承 WebMvcConfigurationSupport，实现他预留的模板方法进行扩展
 */
/*@EnableWebMvc
@Configuration*/
public class MvcExtendConfiguration implements WebMvcConfigurer {

	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {

		registry.viewResolver(new MyViewResolver());
		// 若要实现自定义视图解析器和默认的视图解析器同时生效，可以修改源码的判断条件，或者自定义视图解析器的时候，再手动注册一个默认的视图解析器
		// registry.viewResolver(new InternalResourceViewResolver());
	}
}
