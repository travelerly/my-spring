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

package org.springframework.context.annotation;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.config.AopConfigUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Registers an auto proxy creator against the current {@link BeanDefinitionRegistry}
 * as appropriate based on an {@code @Enable*} annotation having {@code mode} and
 * {@code proxyTargetClass} attributes set to the correct values.
 *
 * @author Chris Beams
 * @since 3.1
 * @see org.springframework.cache.annotation.EnableCaching
 * @see org.springframework.transaction.annotation.EnableTransactionManagement
 */
public class AutoProxyRegistrar implements ImportBeanDefinitionRegistrar {

	private final Log logger = LogFactory.getLog(getClass());

	/**
	 * Register, escalate, and configure the standard auto proxy creator (APC) against the
	 * given registry. Works by finding the nearest annotation declared on the importing
	 * {@code @Configuration} class that has both {@code mode} and {@code proxyTargetClass}
	 * attributes. If {@code mode} is set to {@code PROXY}, the APC is registered; if
	 * {@code proxyTargetClass} is set to {@code true}, then the APC is forced to use
	 * subclass (CGLIB) proxying.
	 * <p>Several {@code @Enable*} annotations expose both {@code mode} and
	 * {@code proxyTargetClass} attributes. It is important to note that most of these
	 * capabilities end up sharing a {@linkplain AopConfigUtils#AUTO_PROXY_CREATOR_BEAN_NAME
	 * single APC}. For this reason, this implementation doesn't "care" exactly which
	 * annotation it finds -- as long as it exposes the right {@code mode} and
	 * {@code proxyTargetClass} attributes, the APC can be registered and configured all
	 * the same.
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		boolean candidateFound = false;
		/**
		 * 这里面需要特别注意的是：这里是拿到所有的注解类型，而不是只拿 @EnableAspectJAutoProxy 这个类型的
		 * 原因：因为 mode、proxyTargetClass 等属性会直接影响到代理的方式，而拥有这些属性的注解至少有：
		 * 		@EnableTransactionManagement、@EnableAsync、@EnableCaching等~~~~
		 * 		甚至还有启用 AOP 的注解：@EnableAspectJAutoProxy 它也能设置 "proxyTargetClass" 这个属性的值，因此也会产生关联影响
		 */
		Set<String> annTypes = importingClassMetadata.getAnnotationTypes();
		for (String annType : annTypes) {
			AnnotationAttributes candidate = AnnotationConfigUtils.attributesFor(importingClassMetadata, annType);
			if (candidate == null) {
				continue;
			}
			/**
			 * 拿到注解例的这两个属性
			 * 如果是 @Configuration 或者别的注解的话，属性可能为 null
			 */
			Object mode = candidate.get("mode");
			Object proxyTargetClass = candidate.get("proxyTargetClass");

			/**
			 * 如果存在属性 mode 和 proxyTargetClass，并且两个属性的 class 类型也对应的上，才会进入此逻辑
			 */
			if (mode != null && proxyTargetClass != null && AdviceMode.class == mode.getClass() &&
					Boolean.class == proxyTargetClass.getClass()) {

				// 标志找到了候选注解
				candidateFound = true;
				if (mode == AdviceMode.PROXY) {
					/**
					 * 会给容器中注册一个 bean 的后置处理器：InfrastructureAdvisorAutoProxyCreator
					 * 类似于 AOP 为容器中注册的：AspectJAwareAdvisorAutoProxyCreator
					 * 若 AOP 和事务同时存在，
					 * 由于 AOP 和事务注册的后置处理器的名字都为 org.springframework.aop.config.internalAutoProxyCreator，
					 * 容器会根据内部维护的优先级来覆盖 beanClass，而 AOP 的优先级更高，即 AOP 会覆盖事务的 beanClass
					 */
					AopConfigUtils.registerAutoProxyCreatorIfNecessary(registry);

					// 判断是否需要强制使用 CGLIB 的方式（若这个属性出现多此，是会以覆盖的形式）
					if ((Boolean) proxyTargetClass) {
						AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
						return;
					}
				}
			}
		}

		/**
		 * 如果一个属性都没有找到，记录日志。
		 * 若是自己注入这个类，而不是使用注解注入，是有可能找不到属性的，但不建议这么做。
		 */
		if (!candidateFound && logger.isInfoEnabled()) {
			String name = getClass().getSimpleName();
			logger.info(String.format("%s was imported but no annotations were found " +
					"having both 'mode' and 'proxyTargetClass' attributes of type " +
					"AdviceMode and boolean respectively. This means that auto proxy " +
					"creator registration and configuration may not have occurred as " +
					"intended, and components may not be proxied as expected. Check to " +
					"ensure that %s has been @Import'ed on the same class where these " +
					"annotations are declared; otherwise remove the import of %s " +
					"altogether.", name, name, name));
		}
	}

}
