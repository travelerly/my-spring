package com.colin.processor.factory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @author colin
 * @create 2021-03-17 14:14
 *
 * BeanFactory 后置处理器有两种实现方式
 * 1.实现 BeanDefinitionRegistryPostProcessor 接口
 * 2.实现 BeanPostProcessor 接口
 * 执行顺序是，BeanDefinitionRegistryPostProcessor 实现类先执行，BeanPostProcessor 实现类后执行
 *
 * 执行逻辑：
 * BeanDefinitionRegistryPostProcessor（先执行）
 * 1.若实现类同时实现了 PriorityOrdered 接口，则优先级最高
 * 2.若实现类同时实现了 Order 接口，重写的 getOrder() 方法的返回值越小，优先级越高
 * 3.若没有实现其他优先级接口，则会按照其配置(加载)顺序执行
 *
 * BeanFactory
 * 1.若实现类同时实现了 PriorityOrdered 接口，则优先级最高
 * 2.若实现类同时实现了 Order 接口，重写的 getOrder() 方法的返回值越小，优先级越高
 * 3.若没有实现其他优先级接口，则会按照其配置(加载)顺序执行
 *
 *
 */
//@Component
public class MyBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

	public MyBeanDefinitionRegistryPostProcessor() {
		System.out.println("====MyBeanDefinitionRegistryPostProcessor====");
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		System.out.println("====MyBeanDefinitionRegistryPostProcessor...postProcessBeanFactory====");
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		System.out.println("====MyBeanDefinitionRegistryPostProcessor...postProcessBeanDefinitionRegistry====");
	}
}
