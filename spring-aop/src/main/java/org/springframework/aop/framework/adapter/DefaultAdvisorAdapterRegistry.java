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

package org.springframework.aop.framework.adapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;

import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;

/**
 * Default implementation of the {@link AdvisorAdapterRegistry} interface.
 * Supports {@link org.aopalliance.intercept.MethodInterceptor},
 * {@link org.springframework.aop.MethodBeforeAdvice},
 * {@link org.springframework.aop.AfterReturningAdvice},
 * {@link org.springframework.aop.ThrowsAdvice}.
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class DefaultAdvisorAdapterRegistry implements AdvisorAdapterRegistry, Serializable {

	private final List<AdvisorAdapter> adapters = new ArrayList<>(3);


	/**
	 * Create a new DefaultAdvisorAdapterRegistry, registering well-known adapters.
	 * 构造方法中初始化适配器集合 adapters
	 */
	public DefaultAdvisorAdapterRegistry() {
		registerAdvisorAdapter(new MethodBeforeAdviceAdapter());
		registerAdvisorAdapter(new AfterReturningAdviceAdapter());
		registerAdvisorAdapter(new ThrowsAdviceAdapter());
	}


	@Override
	public Advisor wrap(Object adviceObject) throws UnknownAdviceTypeException {

		// 如果 adviceObject 本来就是 Advisor 类型，则无需封装，直接返回
		if (adviceObject instanceof Advisor) {
			return (Advisor) adviceObject;
		}

		// 至此，说明 adviceObject 不是 Advisor 类型，判断是否是 Advice 类型，若不是，则直接抛出异常
		if (!(adviceObject instanceof Advice)) {
			throw new UnknownAdviceTypeException(adviceObject);
		}

		Advice advice = (Advice) adviceObject;

		// 若 adviceObject 是 Advice 类型，那么直接使用 DefaultPointcutAdvisor 类进行包装
		if (advice instanceof MethodInterceptor) {
			// So well-known it doesn't even need an adapter.
			return new DefaultPointcutAdvisor(advice);
		}

		// 对 Advisor 适配器的包装
		for (AdvisorAdapter adapter : this.adapters) {
			// Check that it is supported.
			if (adapter.supportsAdvice(advice)) {
				return new DefaultPointcutAdvisor(advice);
			}
		}

		// 其它情况抛出异常
		throw new UnknownAdviceTypeException(advice);
	}

	@Override
	public MethodInterceptor[] getInterceptors(Advisor advisor) throws UnknownAdviceTypeException {
		List<MethodInterceptor> interceptors = new ArrayList<>(3);

		/**
		 * 将切面中的增强方法统一封装到 Advice 中
		 * 前面构建 advisor 时，使用的是类 InstantiationModelAwarePointcutAdvisorImpl 的实例
		 * 是通过其构造方法，将增强方法(candidateAdviceMethod)和切点(expressionPointcut)等信息注入到 Advisor 中的
		 */
		Advice advice = advisor.getAdvice();

		/**
		 * 将 MethodInterceptor 类型的增强器 advice 添加到拦截器中
		 * AOP 增强器中的 Around、After、Before、AfterReturning 这三种类型会在此处被添加到拦截器中
		 */
		if (advice instanceof MethodInterceptor) {
			interceptors.add((MethodInterceptor) advice);
		}

		/**
		 * 将符合条件的增强器添加至拦截器中
		 * AOP 增强器中的 Before、AfterReturning 这两种类型会在此处被添加到拦截器中
		 * this.adapters：List<AdvisorAdapter> 适配器集合，默认大小为 3
		 *     初始化时，就注册了 MethodBeforeAdviceAdapter、AfterReturningAdviceAdapter、ThrowsAdviceAdapter 3 个适配器
		 * 循环适配器，分别处理 @Before、@AfterReturning 这两个增强器，添加进拦截器中
		 */
		for (AdvisorAdapter adapter : this.adapters) {
			if (adapter.supportsAdvice(advice)) {
				/**
				 * 由增强器适配器「adapter」，将增强器「advisor，只保存了哪些方法是通知方法的详细信息」转为拦截器「interceptors」，
				 * 拦截器可以反射执行通知方法的逻辑
				 * AOP 增强器中的 Before、AfterReturning 这两种类型会在此处被添加到拦截器中
				 */
				interceptors.add(adapter.getInterceptor(advisor));
			}
		}
		if (interceptors.isEmpty()) {
			throw new UnknownAdviceTypeException(advisor.getAdvice());
		}
		return interceptors.toArray(new MethodInterceptor[0]);
	}

	@Override
	public void registerAdvisorAdapter(AdvisorAdapter adapter) {
		this.adapters.add(adapter);
	}

}
