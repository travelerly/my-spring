package com.colin.processor.bean;

import org.springframework.beans.factory.InitializingBean;

/**
 *
 * 生命周期接口
 *
 * @author colin
 * @create 2021-03-17 14:27
 */
public class MyInitializingBean implements InitializingBean {

	public MyInitializingBean() {
		 System.out.println("====MyInitializingBean====");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("====MyInitializingBean...afterPropertiesSet====");
	}
}
