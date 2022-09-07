/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.aop.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Utility class for handling registration of AOP auto-proxy creators.
 *
 * <p>Only a single auto-proxy creator should be registered yet multiple concrete
 * implementations are available. This class provides a simple escalation protocol,
 * allowing a caller to request a particular auto-proxy creator and know that creator,
 * <i>or a more capable variant thereof</i>, will be registered as a post-processor.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 2.5
 * @see AopNamespaceUtils
 */
public abstract class AopConfigUtils {

	/**
	 * The bean name of the internally managed auto-proxy creator.
	 */
	public static final String AUTO_PROXY_CREATOR_BEAN_NAME =
			"org.springframework.aop.config.internalAutoProxyCreator";

	/**
	 * Stores the auto proxy creator classes in escalation order.
	 */
	private static final List<Class<?>> APC_PRIORITY_LIST = new ArrayList<>(3);

	/**
	 * 用于判断 AOP 和 TX 注册的后置处理器的优先级列表
	 * 可见 AOP 的后置处理器的索引要大于 TX 的后置处理器的索引
	 * 同时开启 AOP 和 TX 的话，AOP 的后置处理器会覆盖 TX 的后置处理器
	 */
	static {
		// Set up the escalation list...
		// TX 的后置处理器
		APC_PRIORITY_LIST.add(InfrastructureAdvisorAutoProxyCreator.class);
		APC_PRIORITY_LIST.add(AspectJAwareAdvisorAutoProxyCreator.class);
		// AOP 的后置处理器
		APC_PRIORITY_LIST.add(AnnotationAwareAspectJAutoProxyCreator.class);
	}


	@Nullable
	public static BeanDefinition registerAutoProxyCreatorIfNecessary(BeanDefinitionRegistry registry) {
		return registerAutoProxyCreatorIfNecessary(registry, null);
	}

	@Nullable
	public static BeanDefinition registerAutoProxyCreatorIfNecessary(
			BeanDefinitionRegistry registry, @Nullable Object source) {
		// InfrastructureAdvisorAutoProxyCreator
		return registerOrEscalateApcAsRequired(InfrastructureAdvisorAutoProxyCreator.class, registry, source);
	}

	@Nullable
	public static BeanDefinition registerAspectJAutoProxyCreatorIfNecessary(BeanDefinitionRegistry registry) {
		return registerAspectJAutoProxyCreatorIfNecessary(registry, null);
	}

	@Nullable
	public static BeanDefinition registerAspectJAutoProxyCreatorIfNecessary(
			BeanDefinitionRegistry registry, @Nullable Object source) {

		return registerOrEscalateApcAsRequired(AspectJAwareAdvisorAutoProxyCreator.class, registry, source);
	}

	@Nullable
	public static BeanDefinition registerAspectJAnnotationAutoProxyCreatorIfNecessary(BeanDefinitionRegistry registry) {
		return registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry, null);
	}

	@Nullable
	public static BeanDefinition registerAspectJAnnotationAutoProxyCreatorIfNecessary(
			BeanDefinitionRegistry registry, @Nullable Object source) {

		return registerOrEscalateApcAsRequired(AnnotationAwareAspectJAutoProxyCreator.class, registry, source);
	}

	public static void forceAutoProxyCreatorToUseClassProxying(BeanDefinitionRegistry registry) {
		if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
			BeanDefinition definition = registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
			definition.getPropertyValues().add("proxyTargetClass", Boolean.TRUE);
		}
	}

	public static void forceAutoProxyCreatorToExposeProxy(BeanDefinitionRegistry registry) {
		if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
			BeanDefinition definition = registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
			definition.getPropertyValues().add("exposeProxy", Boolean.TRUE);
		}
	}

	/**
	 * AOP 和 TX 都会调用此方法，给容器中注册一个 bean 的后置处理器，传参不同：
	 * AOP：AnnotationAwareAspectJAutoProxyCreator
	 * TX：InfrastructureAdvisorAutoProxyCreator
	 *
	 * @param cls
	 * @param registry
	 * @param source
	 * @return
	 */
	@Nullable
	private static BeanDefinition registerOrEscalateApcAsRequired(
			Class<?> cls, BeanDefinitionRegistry registry, @Nullable Object source) {

		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		/**
		 * 判断容器中是否包含组件 internalAutoProxyCreator。
		 * AOP 和 TX 都会为容器中注册名称为 internalAutoProxyCreator 的 bean 的后置处理器的 BeanDefinition
		 * 容器会根据内部维护的优先级来覆盖 beanClass，即 AOP 会覆盖事务的组件
		 */
		if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
			BeanDefinition apcDefinition = registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);

			// 判断当前类的名称和容器中已有类的名称是否相同，若不相同，则继续判断
			if (!cls.getName().equals(apcDefinition.getBeanClassName())) {
				// 找到容器中已存在的名称为 internalAutoProxyCreator 的 bean 的优先级
				int currentPriority = findPriorityForClass(apcDefinition.getBeanClassName());
				// 找到当前 bean 的优先级
				int requiredPriority = findPriorityForClass(cls);

				if (currentPriority < requiredPriority) {
					/**
					 * 优先级高的会覆盖优先级低的 bean
					 * AopConfigUtils 中有一个静态列表，根据 bean 在列表中的索引来获取其优先级
					 *
					 * static {
					 * 		APC_PRIORITY_LIST.add(InfrastructureAdvisorAutoProxyCreator.class);
					 * 		APC_PRIORITY_LIST.add(AspectJAwareAdvisorAutoProxyCreator.class);
					 * 		APC_PRIORITY_LIST.add(AnnotationAwareAspectJAutoProxyCreator.class);
					 * }
					 *
					 * 根据列表可以得出 AOP 的后置处理器的优先级要高于 TX 的后置处理器的优先级
					 * 所以，同时开启 AOP 和 TX 的话，AOP 的后置处理器会覆盖 TX 的后置处理器
					 */
					apcDefinition.setBeanClassName(cls.getName());
				}
			}
			return null;
		}

		/**
		 * AOP 和 TX 都会给容器中注册一个 beanName 为 internalAutoProxyCreator 的后置处理器的 BeanDefinition，但其优先级不同
		 * 		AOP：cls 为 AnnotationAwareAspectJAutoProxyCreator 的 Class 对象
		 * 		TX：cls 为 InfrastructureAdvisorAutoProxyCreator 的 Class 对象
		 */
		RootBeanDefinition beanDefinition = new RootBeanDefinition(cls);
		beanDefinition.setSource(source);
		beanDefinition.getPropertyValues().add("order", Ordered.HIGHEST_PRECEDENCE);
		beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

		/**
		 * AOP 和 TX 都会给容器中注册 beanName 为 internalAutoProxyCreator 的后置处理器的 BeanDefinition
		 * key：internalAutoProxyCreator
		 * value：
		 * 		AOP->AnnotationAwareAspectJAutoProxyCreator
		 * 		TX->InfrastructureAdvisorAutoProxyCreator
		 *
		 * 	InfrastructureAdvisorAutoProxyCreator 和 AnnotationAwareAspectJAutoProxyCreator 都继承了 AbstractAdvisorAutoProxyCreator
		 * 	InfrastructureAdvisorAutoProxyCreator 重写了 AbstractAdvisorAutoProxyCreator 的 isEligibleAdvisorBean 方法，
		 *  解析切面时，后置处理器仅对容器内部 bean 其作用
		 *
		 *  AnnotationAwareAspectJAutoProxyCreator 没有重写 AbstractAdvisorAutoProxyCreator 的 isEligibleAdvisorBean 方法，
		 *  默认返回 true，即即系切面时，后置处理器对于自定义 bean 也会起作用
		 *
		 */
		registry.registerBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME, beanDefinition);
		return beanDefinition;
	}

	private static int findPriorityForClass(Class<?> clazz) {
		return APC_PRIORITY_LIST.indexOf(clazz);
	}

	private static int findPriorityForClass(@Nullable String className) {
		for (int i = 0; i < APC_PRIORITY_LIST.size(); i++) {
			Class<?> clazz = APC_PRIORITY_LIST.get(i);
			if (clazz.getName().equals(className)) {
				return i;
			}
		}
		throw new IllegalArgumentException(
				"Class name [" + className + "] is not a known auto-proxy creator class");
	}

}
