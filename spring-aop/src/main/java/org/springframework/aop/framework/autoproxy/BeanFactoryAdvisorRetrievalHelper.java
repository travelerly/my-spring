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

package org.springframework.aop.framework.autoproxy;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.Advisor;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Helper for retrieving standard Spring Advisors from a BeanFactory,
 * for use with auto-proxying.
 *
 * @author Juergen Hoeller
 * @since 2.0.2
 * @see AbstractAdvisorAutoProxyCreator
 */
public class BeanFactoryAdvisorRetrievalHelper {

	private static final Log logger = LogFactory.getLog(BeanFactoryAdvisorRetrievalHelper.class);

	private final ConfigurableListableBeanFactory beanFactory;

	@Nullable
	private volatile String[] cachedAdvisorBeanNames;


	/**
	 * Create a new BeanFactoryAdvisorRetrievalHelper for the given BeanFactory.
	 * @param beanFactory the ListableBeanFactory to scan
	 */
	public BeanFactoryAdvisorRetrievalHelper(ConfigurableListableBeanFactory beanFactory) {
		Assert.notNull(beanFactory, "ListableBeanFactory must not be null");
		this.beanFactory = beanFactory;
	}


	/**
	 * Find all eligible Advisor beans in the current bean factory,
	 * ignoring FactoryBeans and excluding beans that are currently in creation.
	 * @return the list of {@link org.springframework.aop.Advisor} beans
	 * @see #isEligibleBean
	 */
	public List<Advisor> findAdvisorBeans() {
		/**
		 * 探测器字段缓存 cachedAdvisorBeanNames，用来保存 Advisor 的全类名
		 * 会在创建第一个单实例 bean 中把 Advisor 名称解析出来
		 */
		String[] advisorNames = this.cachedAdvisorBeanNames;
		if (advisorNames == null) {
			/**
			 * 从容器中获取到实现了 Advisor 接口的实现类
			 * 事务注解 @EnableTransactionManagement 导入了一个叫 ProxyTransactionManagementConfiguration 的配置类
			 * 这个配置类中配置了：
			 * @Bean(name = TransactionManagementConfigUtils.TRANSACTION_ADVISOR_BEAN_NAME)
			 * @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
			 * public BeanFactoryTransactionAttributeSourceAdvisor transactionAdvisor() ……
			 * 把他的名字获取出来保存到本类的属性变量 cachedAdvisorBeanNames 中，即保存到缓存中
			 *
			 * Do not initialize FactoryBeans here: We need to leave all regular beans
			 * uninitialized to let the auto-proxy creator apply to them!
			 */
			advisorNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
					this.beanFactory, Advisor.class, true, false);
			this.cachedAdvisorBeanNames = advisorNames;
		}

		// 若容器中没有找到，则直接返回一个空集合
		if (advisorNames.length == 0) {
			return new ArrayList<>();
		}

		List<Advisor> advisors = new ArrayList<>();
		// 遍历找到的容器中配置的 BeanFactoryTransactionAttributeSourceAdvisor
		for (String name : advisorNames) {
			/**
			 * 判断当前 BeanFactoryTransactionAttributeSourceAdvisor 是否符合要求
			 * isEligibleBean() 为 InfrastructureAdvisorAutoProxyCreator 重写的方法，
			 * 用于判断，当前 name 对应的 bean 是否为内部 bean，如果是内部 bean 则符合要求，返回 true，
			 * 即当前的后置处理器是内部 bean，才会被解析为一个 Advisor
			 *
			 * 但如果同时开启了 AOP 和 TX，AOP 的后置处理器 会覆盖 TX 的后置处理器，
			 * 而 AOP 的后置处理器没有从写 isEligibleBean() 方法，因此会返回默认值 true，
			 * 即默认会将当前 name 对应的后置处理器解析成 Advisor
			 */
			if (isEligibleBean(name)) {
				// 判断当前 BeanFactoryTransactionAttributeSourceAdvisor 是否正在创建中
				if (this.beanFactory.isCurrentlyInCreation(name)) {
					if (logger.isTraceEnabled()) {
						logger.trace("Skipping currently created advisor '" + name + "'");
					}
				}
				else {
					try {
						// 调用 getBean 方法创建 BeanFactoryTransactionAttributeSourceAdvisor 的对象
						advisors.add(this.beanFactory.getBean(name, Advisor.class));
					}
					catch (BeanCreationException ex) {
						Throwable rootCause = ex.getMostSpecificCause();
						if (rootCause instanceof BeanCurrentlyInCreationException) {
							BeanCreationException bce = (BeanCreationException) rootCause;
							String bceBeanName = bce.getBeanName();
							if (bceBeanName != null && this.beanFactory.isCurrentlyInCreation(bceBeanName)) {
								if (logger.isTraceEnabled()) {
									logger.trace("Skipping advisor '" + name +
											"' with dependency on currently created bean: " + ex.getMessage());
								}
								// Ignore: indicates a reference back to the bean we're trying to advise.
								// We want to find advisors other than the currently created bean itself.
								continue;
							}
						}
						throw ex;
					}
				}
			}
		}
		return advisors;
	}

	/**
	 * Determine whether the aspect bean with the given name is eligible.
	 * <p>The default implementation always returns {@code true}.
	 * @param beanName the name of the aspect bean
	 * @return whether the bean is eligible
	 */
	protected boolean isEligibleBean(String beanName) {
		return true;
	}

}
