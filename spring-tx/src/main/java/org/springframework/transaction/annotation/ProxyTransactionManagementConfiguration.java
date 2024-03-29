/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.transaction.annotation;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.config.TransactionManagementConfigUtils;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * {@code @Configuration} class that registers the Spring infrastructure beans
 * necessary to enable proxy-based annotation-driven transaction management.
 *
 * @author Chris Beams
 * @author Sebastien Deleuze
 * @since 3.1
 * @see EnableTransactionManagement
 * @see TransactionManagementConfigurationSelector
 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class ProxyTransactionManagementConfiguration extends AbstractTransactionManagementConfiguration {

	/**
	 * 导入了关于事务的切面信息
	 * 这个 Advisor 是事务的核心内容
	 * @param transactionAttributeSource
	 * @param transactionInterceptor
	 * @return
	 */
	@Bean(name = TransactionManagementConfigUtils.TRANSACTION_ADVISOR_BEAN_NAME)
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE) // 角色设置为 2，即角色为内部类
	public BeanFactoryTransactionAttributeSourceAdvisor transactionAdvisor(
			TransactionAttributeSource transactionAttributeSource, TransactionInterceptor transactionInterceptor) {
		// 事务的 Advisor 中内置了 Advice

		// 创建 Advisor
		BeanFactoryTransactionAttributeSourceAdvisor advisor = new BeanFactoryTransactionAttributeSourceAdvisor();
		// 设置用于解析 @Transactional 注解的事务源对象
		advisor.setTransactionAttributeSource(transactionAttributeSource);
		// 设置 Advice 对象
		advisor.setAdvice(transactionInterceptor);

		// 顺序由 @EnableTransactionManagement 注解的 Order 属性来指定，默认为：Ordered.LOWEST_PRECEDENCE
		if (this.enableTx != null) {
			advisor.setOrder(this.enableTx.<Integer>getNumber("order"));
		}
		return advisor;
	}

	/**
	 * 导入的事务属性源对象：用于解析 @Transactional 注解
	 * @return AnnotationTransactionAttributeSource：基于注解的事务属性源对象
	 */
	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public TransactionAttributeSource transactionAttributeSource() {
		return new AnnotationTransactionAttributeSource();
	}

	/**
	 * 用户拦截事务方法执行的拦截器
	 * 它是一个 MethodInterceptor，它也是 Spring 处理事务最核心的部分
	 * 也可以自定义一个 TransactionInterceptor(同名)，来覆盖此 Bean(注意是覆盖)
	 * 注意自定义的 BeanName 必须同名，也就是必须为 transactionInterceptor，否则两个都会注册进容器中
	 * @param transactionAttributeSource 基于注解的事务属性源对象
	 * @return 用户拦截事务方法执行的拦截器
	 */
	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public TransactionInterceptor transactionInterceptor(TransactionAttributeSource transactionAttributeSource) {
		TransactionInterceptor interceptor = new TransactionInterceptor();
		// 设置事务的属性
		interceptor.setTransactionAttributeSource(transactionAttributeSource);
		/**
		 * 事务管理器，也就是注解最终需要使用的事务管理器，父类已经处理好了
		 * 此处注意：是可以不用特殊指定的，最终它自己会去容器中匹配一个合适的
		 */
		if (this.txManager != null) {
			interceptor.setTransactionManager(this.txManager);
		}
		return interceptor;
	}

}
