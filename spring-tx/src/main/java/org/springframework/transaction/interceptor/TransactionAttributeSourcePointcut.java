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

package org.springframework.transaction.interceptor;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.lang.Nullable;
import org.springframework.transaction.TransactionManager;
import org.springframework.util.ObjectUtils;

/**
 * 继承 StaticMethodMatcherPointcut，从而继承 StaticMethodMatcher
 * 所以 "classFilter = ClassFilter.TRUE;" 匹配所有的类；并且 isRuntime = false，表示只需要对方法进行静态匹配即可
 *
 * Abstract class that implements a Pointcut that matches if the underlying
 * {@link TransactionAttributeSource} has an attribute for a given method.
 *
 * @author Juergen Hoeller
 * @since 2.5.5
 */
@SuppressWarnings("serial")
abstract class TransactionAttributeSourcePointcut extends StaticMethodMatcherPointcut implements Serializable {

	protected TransactionAttributeSourcePointcut() {
		setClassFilter(new TransactionAttributeSourceClassFilter());
	}

	/**
	 * 方法的匹配，静态匹配即可(因为事务无需动态匹配这么细的粒度)
	 * 此方法的调用时机：主要是容器内的 Bean，都会通过 AbstractAutoProxyCreator#postProcessAfterInitialization()
	 * 从而会调用 wrapIfNecessary 方法，因此容器内所有的 Bean 的所有方法在容器启动时候都会执行此 matche() 方法，因此请注意缓存的使用
	 * @param method the candidate method
	 * @param targetClass the target class
	 * @return
	 */
	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		/**
		 * 获取 @EnableTransactionManagement 注解导入的
		 * 配置类 ProxyTransactionManagementConfiguration 中配置的 TransactionAttributeSource 对象
		 * 即获取配置类中的事务属性源对象，用于解析 @Transactional 注解
		 *
		 * getTransactionAttributeSource()：抽象方法，这里获得到的是通过 @Import 导入的 ImportSelect 注册的
		 * 配置类 ProxyTransactionManagementConfiguration 中设置的 AnnotationTransactionAttributeSource，
		 * 它是基于注解驱动的事务管理的事务属性源，和 @Transaction 相关，也是现在使用得最最多的方式，
		 * 它的基本作用为：它遇上比如 @Transaction 标注的方法时，此类会分析此事务注解，最终组织形成一个 TransactionAttribute 供随后的调用
		 *
		 * 如果事务属性源对象为 null，则表示没有配置事务属性源，那是全部匹配的，也就是说所有的方法都匹配
		 * 或者标注了 @Transactional 这样的注解的方法才会给与匹配
		 */
		TransactionAttributeSource tas = getTransactionAttributeSource();
		// 通过 getTransactionAttribute() 方法判断是否标注了 @Transactional 注解
		return (tas == null || tas.getTransactionAttribute(method, targetClass) != null);
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof TransactionAttributeSourcePointcut)) {
			return false;
		}
		TransactionAttributeSourcePointcut otherPc = (TransactionAttributeSourcePointcut) other;
		return ObjectUtils.nullSafeEquals(getTransactionAttributeSource(), otherPc.getTransactionAttributeSource());
	}

	@Override
	public int hashCode() {
		return TransactionAttributeSourcePointcut.class.hashCode();
	}

	@Override
	public String toString() {
		return getClass().getName() + ": " + getTransactionAttributeSource();
	}


	/**
	 * Obtain the underlying TransactionAttributeSource (may be {@code null}).
	 * To be implemented by subclasses.
	 */
	@Nullable
	protected abstract TransactionAttributeSource getTransactionAttributeSource();


	/**
	 * {@link ClassFilter} that delegates to {@link TransactionAttributeSource#isCandidateClass}
	 * for filtering classes whose methods are not worth searching to begin with.
	 */
	private class TransactionAttributeSourceClassFilter implements ClassFilter {

		@Override
		public boolean matches(Class<?> clazz) {
			if (TransactionalProxy.class.isAssignableFrom(clazz) ||
					TransactionManager.class.isAssignableFrom(clazz) ||
					PersistenceExceptionTranslator.class.isAssignableFrom(clazz)) {
				return false;
			}
			TransactionAttributeSource tas = getTransactionAttributeSource();
			return (tas == null || tas.isCandidateClass(clazz));
		}
	}

}
