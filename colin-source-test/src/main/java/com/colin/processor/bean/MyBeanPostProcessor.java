package com.colin.processor.bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 *
 * Bean组件的后置增强处理器
 *
 * @author colin
 * @create 2021-03-17 14:20
 */
@Component
public class MyBeanPostProcessor implements BeanPostProcessor {

	public MyBeanPostProcessor() {
		System.out.println("====MyBeanPostProcessor=====");
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		System.out.println("MyBeanPostProcessor...postProcessAfterInitialization..."+bean+"==>"+beanName);
		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		System.out.println("MyBeanPostProcessor...postProcessBeforeInitialization..."+bean+"==>"+beanName);
		return bean;
	}
}
