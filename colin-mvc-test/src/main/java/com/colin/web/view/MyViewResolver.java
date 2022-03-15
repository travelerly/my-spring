package com.colin.web.view;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.util.Locale;

/**
 * @author colin
 * @create 2021-05-04 21:24
 * 自定义视图解析器
 * SpringBoot 整合时，自定义视图解析器需要制定好排序，
 * 防止因某个返回值满足多种视图解析规则，排在前面的视图解析器解析成功，就直接返回，造成只存在排序靠前的解析器。
 */
/*@Order
@Component*/
public class MyViewResolver implements ViewResolver{

	@Override
	public View resolveViewName(String viewName, Locale locale) throws Exception {
		// 只解析自定义视图 myView
		if (viewName.startsWith("myView:")){
			return new MyView();
		}
		return null;
	}
}
