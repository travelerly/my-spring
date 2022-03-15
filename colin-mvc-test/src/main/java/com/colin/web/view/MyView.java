package com.colin.web.view;

import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author colin
 * @create 2021-05-04 21:13
 * 自定义视图
 */
public class MyView implements View {

	@Override
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// 自定义视图渲染逻辑
		StringBuffer stringBuffer = new StringBuffer();
		String name = request.getParameter("name");
		stringBuffer.append("<h1>"+name+"</h1>");
		stringBuffer.append("<h2>"+model.get("info")+"</h2>");
		stringBuffer.append("<img src='https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=1207128855,3080267973&fm=26&gp=0.jpg'>");
		response.getWriter().write(stringBuffer.toString());
	}

}
