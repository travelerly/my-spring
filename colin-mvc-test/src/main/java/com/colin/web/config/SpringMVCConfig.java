package com.colin.web.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Controller;

/**
 * @author colin
 * @create 2021-05-02 15:12
 * SpringMVC 只扫描 Controller 组件,这样形成了父子容器。
 * 也可以不指定父容器类，让 MVC 扫描所有，也就没有父子容器了，只有一个容器，这样 @Component + @RequestMapping 就生效了。
 *
 */
@ComponentScan(value = "com.colin.web",includeFilters = {
		@ComponentScan.Filter(type = FilterType.ANNOTATION,value = Controller.class)
},useDefaultFilters = false)
public class SpringMVCConfig {
	// 这是 SpringMVC 的子容器，能扫描到父容器（Spring）中的组件
}
