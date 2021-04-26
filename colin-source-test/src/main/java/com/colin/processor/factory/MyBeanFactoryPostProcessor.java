package com.colin.processor.factory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 *
 * BeanFactory的后置处理器
 *
 * @author colin
 * @create 2021-03-17 14:13
 */
//@Component
public class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	public MyBeanFactoryPostProcessor() {
		System.out.println("====MyBeanFactoryPostProcessor====");
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		System.out.println("====MyBeanFactoryPostProcessor...postProcessBeanFactory====");
	}
}
