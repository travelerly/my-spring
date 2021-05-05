package com.colin.web.view;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.util.Locale;

/**
 * @author colin
 * @create 2021-05-04 21:24
 * 自定义视图解析器
 */
//@Component
public class MeiNvResolver implements ViewResolver {
	@Override
	public View resolveViewName(String viewName, Locale locale) throws Exception {
		// 直解析 meinv
		if (viewName.startsWith("meinv:")){
			return new MeiNvView();
		}
		return null;
	}
}
