package com.colin.web.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Controller;

/**
 * @author colin
 * @create 2021-04-26 15:47
 * Spring 不扫描 Controller 组件
 */
@ComponentScan(value = "com.colin.web",excludeFilters = {
		@ComponentScan.Filter(type = FilterType.ANNOTATION,value = Controller.class)
})
public class MySpringConfig {
	// 这个 Spring 的父容器
}
