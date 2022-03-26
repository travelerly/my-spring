/*
 * Copyright 2002-2021 the original author or authors.
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

package org.springframework.context.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.StartupStep;
import org.springframework.lang.Nullable;

/**
 * Delegate for AbstractApplicationContext's post-processor handling.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 4.0
 */
final class PostProcessorRegistrationDelegate {

	private PostProcessorRegistrationDelegate() {
	}

	// 执行工厂的后置处理器
	public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		// WARNING: Although it may appear that the body of this method can be easily
		// refactored to avoid the use of multiple loops and multiple lists, the use
		// of multiple lists and multiple passes over the names of processors is
		// intentional. We must ensure that we honor the contracts for PriorityOrdered
		// and Ordered processors. Specifically, we must NOT cause processors to be
		// instantiated (via getBean() invocations) or registered in the ApplicationContext
		// in the wrong order.
		//
		// Before submitting a pull request (PR) to change this method, please review the
		// list of all declined PRs involving changes to PostProcessorRegistrationDelegate
		// to ensure that your proposal does not result in a breaking change:
		// https://github.com/spring-projects/spring-framework/issues?q=PostProcessorRegistrationDelegate+is%3Aclosed+label%3A%22status%3A+declined%22

		// Invoke BeanDefinitionRegistryPostProcessors first, if any.
		Set<String> processedBeans = new HashSet<>();

		// 判断 BeanFactory 是否是 BeanDefinitionRegistry 接口的实现类
		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			// 存储普通的 BeanFactoryPostProcessor 工厂后置处理器
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
			// 存储 BeanDefinitionRegistryPostProcessor 类型的 BeanFactoryPostProcessor 工厂后置处理器
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

			// 先拿到底层默认的所有的工厂后置处理器 beanFactoryPostProcessor 进行遍历（默认为空）
			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					// 处理 BeanDefinitionRegistryPostProcessor 类型的工厂后置处理器
					// 后置处理器类型转换
					BeanDefinitionRegistryPostProcessor registryProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
					// 首先执行 BeanDefinitionRegistryPostProcessor 中的 postProcessBeanDefinitionRegistry()
					registryProcessor.postProcessBeanDefinitionRegistry(registry);
					// 然后再将 registryProcessor 存储起来，方便后续执行 postProcessBeanFactory() 方法
					registryProcessors.add(registryProcessor);
				}
				else {
					// 存储普通的工厂后置处理器
					regularPostProcessors.add(postProcessor);
				}
			}

			// Do not initialize FactoryBeans here: We need to leave all regular beans
			// uninitialized to let the bean factory post-processors apply to them!
			// Separate between BeanDefinitionRegistryPostProcessors that implement
			// PriorityOrdered, Ordered, and the rest.
			// 用于保存当前需要执行的 BeanDefinitionRegistryPostProcessor（每处理完一批，会阶段性清除一批）
			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			/**
			 * 首先；从工厂中获取所有的既实现了优先级（PriorityOrdered）接口，又实现了 BeanDefinitionRegistryPostProcessor 接口的实现类的名字
			 * First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
			 */
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					// 从容器中获得这个后置处理器「getBean 整个创建过程」，并放入这个集合中
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}

			// 利用优先级排序
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			// 添加到 registryProcessors 中，方便后续执行方法 postProcessBeanFactory
			registryProcessors.addAll(currentRegistryProcessors);
			/**
			 * 执行 BeanDefinitionRegistryPostProcessor 中的 postProcessBeanDefinitionRegistry() 方法
			 * 即执行 BeanDefinitionRegistry 的后置处理功能
			 */
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());
			// 执行完成后，清空集合，准备下一轮
			currentRegistryProcessors.clear();

			/**
			 * 接下来，从工厂中获取所有既实现了排序（Ordered）接口，又实现了 BeanDefinitionRegistryPostProcessor 接口的实现类的名字。
			 * Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
			 */
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					// 从容器中获得这个组件「getBean 整个创建过程」，并放入这个集合中
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}

			// 排序 (实现 Ordered 接口)
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);

			/**
			 * 执行 BeanDefinitionRegistryPostProcessor 中的 postProcessBeanDefinitionRegistry() 方法
			 * 即执行 BeanDefinitionRegistry 的后置处理功能
			 */
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());
			// 清空集合，准备下一轮
			currentRegistryProcessors.clear();

			/**
			 * 最后，没有实现任何排序及优先级接口的情况，从容器中拿到所有 BeanDefinitionRegistryPostProcessor 接口的实现类的名字
			 * Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
			 */
			boolean reiterate = true;
			// 直到 postProcessorName 为空，reiterate 为 false，在退出 while 循环
			while (reiterate) {
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				for (String ppName : postProcessorNames) {
					if (!processedBeans.contains(ppName)) {
						// 从容器中获得这个组件「getBean 整个创建过程」，并放入这个集合中
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						reiterate = true;
					}
				}
				// 此处没有实现排序接口，则根据类名首字母大小写进行排序
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				registryProcessors.addAll(currentRegistryProcessors);
				/**
				 * 执行 BeanDefinitionRegistryPostProcessor 中的 postProcessBeanDefinitionRegistry() 方法
				 * 即执行 BeanDefinitionRegistry 的后置处理功能
				 */
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());
				// 清空集合，防止重复
				currentRegistryProcessors.clear();
			}

			/**
			 * 统一调用 BeanDefinitionRegistryPostProcessor 类型的工厂后置处理器的 postProcessBeanFactory 方法
			 * registryProcessors 中存储 BeanDefinitionRegistryPostProcessor 类型的 BeanFactoryPostProcessor 工厂后置处理器
			 * Now, invoke the postProcessBeanFactory callback of all processors handled so far.
			 */
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
			/**
			 * 统一调用普通类型的工厂后置处理器的 postProcessBeanFactory 方法
			 * regularPostProcessors 中存储普通的 BeanFactoryPostProcessor 工厂后置处理器
			 */
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
		}

		// 以上执行的是 BeanDefinitionRegistryPostProcessor 类型的后置处理器

		else {
			/**
			 * 从此处开始执行 BeanFactoryPostProcessor 类型的后置处理器的 postProcessBeanFactory 方法
			 * Invoke factory processors registered with the context instance.
			 */
			invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
		}

		/**
		 * 以上环节，参数 beanFactoryPostProcessors，以及工厂中所有类型为 BeanDefinitionRegistryPostProcessor 的 bean 就已经全部都处理完成了。
		 * 接下来处理工厂中存粹只实现接口 BeanFactoryPostProcessor 的 bean
		 */

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the bean factory post-processors apply to them!
		// 从工厂中获取所有实现 BeanFactoryPostProcessor 接口的类的名字
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		// 存储实现了优先级（PriorityOrdered）接口的 BeanFactoryPostProcessor 的实现类
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		// 存储实现了排序接口（Ordered）接口的 BeanFactoryPostProcessor 的实现类
		List<String> orderedPostProcessorNames = new ArrayList<>();
		// 存放无序的（即未实现 PriorityOrdered 接口和 Ordered 接口）的 BeanFactoryPostProcessor 的实现类名称
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (processedBeans.contains(ppName)) {
				/**
				 * 直接空实现，因为 processedBeans 中，已经记录了前面处理过的 BeanDefinitionRegistryPostProcessor 的 bean 的名称
				 * 表示已经处理过了，不再重复处理
				 * skip - already processed in first phase above
				 */
			}
			else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		/**
		 * 利用优先级排序（实现了 PriorityOrdered 接口）
		 * First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
		 */
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		// 执行工厂后置处理器中的 postProcessBeanFactory 方法
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// 接下来，从工厂中获取所有的实现了 Ordered 接口的 BeanFactoryPostProcessor。 Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		// 根据 Ordered 接口排序
		sortPostProcessors(orderedPostProcessors, beanFactory);
		// 执行工厂后置处理器的 postProcessBeanFactory 方法
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// 最后，没有实现任何优先级或排序接口的情况，从容器中拿到所有 BeanFactoryPostProcessor。 Finally, invoke all other BeanFactoryPostProcessors.
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		// 执行工厂后置处理器的 postProcessBeanFactory 方法
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

		// Clear cached merged bean definitions since the post-processors might have
		// modified the original metadata, e.g. replacing placeholders in values...
		// 清除元数据相关的缓存，后置处理器可能已经修改了原始的一些元数据
		beanFactory.clearMetadataCache();
	}

	/**
	 *
	 * 获取所有的后置处理器，并按照以下几种方式分类，并重进行新排序和注册进工厂；最后将 ApplicationListenerDetector 放到后置处理器容器的最后一位。
	 * 1.实现了优先级接口(PriorityOrdered)的后置处理器
	 * 2.实现了排序接口(Ordered)的后置处理器
	 * 3.没有实现以上两种接口的后置处理器
	 * 4.MergedBeanDefinitionPostProcessor 类型的后置处理器
	 *
	 * @param beanFactory
	 * @param applicationContext
	 */
	public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {

		// WARNING: Although it may appear that the body of this method can be easily
		// refactored to avoid the use of multiple loops and multiple lists, the use
		// of multiple lists and multiple passes over the names of processors is
		// intentional. We must ensure that we honor the contracts for PriorityOrdered
		// and Ordered processors. Specifically, we must NOT cause processors to be
		// instantiated (via getBean() invocations) or registered in the ApplicationContext
		// in the wrong order.
		//
		// Before submitting a pull request (PR) to change this method, please review the
		// list of all declined PRs involving changes to PostProcessorRegistrationDelegate
		// to ensure that your proposal does not result in a breaking change:
		// https://github.com/spring-projects/spring-framework/issues?q=PostProcessorRegistrationDelegate+is%3Aclosed+label%3A%22status%3A+declined%22
		// 从容器中获取所有 BeanPostProcessor 类型的后置处理器的名字集合
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
		// 计算容器中 bean 后置处理器的数量（已经存在 + 即将要注册的 + 容器中还没有注册的）
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		// 检测哪些 bean 没有资格被 bean 后置处理器所处理，记录相应的日志信息
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		// Separate between BeanPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		// 实现了优先级接口(PriorityOrdered)的后置处理器集合
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		// 存放 Spring 内部的 bean 后置处理器集合
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
		// 实现了排序接口(Ordered)的后置处理器名称集合
		List<String> orderedPostProcessorNames = new ArrayList<>();
		// 存放无序的（即没有实现优先级或者排序接口的）后置处理器的名称的集合
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();

		// 遍历所有 bean 后置处理器的名称
		for (String ppName : postProcessorNames) {
			// 根据后置处理器名字，分类型进行拆分集合。
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				// 从容器中获取实现了优先级接口的后置处理器对象。（创建对象）
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				// 添加到优先级接口集合
				priorityOrderedPostProcessors.add(pp);
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					// 筛选出 MergedBeanDefinitionPostProcessor 类型的后置处理器。与实例化注解关系密切，如 @Autowired
					internalPostProcessors.add(pp);
				}
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				// 筛选出所有实现了排序接口(Ordered)的后置处理器名字
				orderedPostProcessorNames.add(ppName);
			}
			else {
				// 筛选出所有无序（即没有实现优先级接口和排序接口的）的后置处理器名字
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// 首先对实现了优先级接口(PriorityOrdered)的后置处理器集合进行排序。First, register the BeanPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		// 将实现了优先级接口(PriorityOrdered)的后置处理器集合注册进工厂
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// 获取所有实现了排序接口(Ordered)的后置处理器，例如 Aop 导入的 AnnotationAwareAspectJAutoProxyCreator。 Next, register the BeanPostProcessors that implement Ordered.
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String ppName : orderedPostProcessorNames) {
			// 从容器中获取实现了 Ordered 接口的后置处理器对象。（创建对象）
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				// 筛选出 MergedBeanDefinitionPostProcessor 类型的后置处理器
				internalPostProcessors.add(pp);
			}
		}
		// 对实现了排序接口(Ordered)的后置处理器集合进行排序
		sortPostProcessors(orderedPostProcessors, beanFactory);
		// 将实现了排序接口(Ordered)的后置处理器集合注册进工厂
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// 获取所有没有实现优先级接口(PriorityOrdered)和没有实现排序接口(Ordered)的后置处理器。Now, register all regular BeanPostProcessors.
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String ppName : nonOrderedPostProcessorNames) {
			// 从容器中获取没有实现优先级接口(PriorityOrdered)和没有实现排序接口(Ordered)的后置处理器对象。（创建对象）
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		// 将所有没有实现优先级接口(PriorityOrdered)和没有实现排序接口(Ordered)的后置处理器注册进工厂
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// 将所有 MergedBeanDefinitionPostProcessor 类型的后置处理器集合进行排序。Finally, re-register all internal BeanPostProcessors.
		sortPostProcessors(internalPostProcessors, beanFactory);
		// 将所有 MergedBeanDefinitionPostProcessor 类型的后置处理器注册进工厂
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		// 重新注册一下这个后置处理器「ApplicationListenerDetector」。Re-register post-processor for detecting inner beans as ApplicationListeners,
		// 把他放到后置处理器容器的最后一位。moving it to the end of the processor chain (for picking up proxies etc).
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
	}

	private static void sortPostProcessors(List<?> postProcessors, ConfigurableListableBeanFactory beanFactory) {
		// Nothing to sort?
		if (postProcessors.size() <= 1) {
			return;
		}
		Comparator<Object> comparatorToUse = null;
		if (beanFactory instanceof DefaultListableBeanFactory) {
			comparatorToUse = ((DefaultListableBeanFactory) beanFactory).getDependencyComparator();
		}
		if (comparatorToUse == null) {
			comparatorToUse = OrderComparator.INSTANCE;
		}
		postProcessors.sort(comparatorToUse);
	}

	/**
	 * Invoke the given BeanDefinitionRegistryPostProcessor beans.
	 */
	private static void invokeBeanDefinitionRegistryPostProcessors(
			Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry, ApplicationStartup applicationStartup) {

		for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
			StartupStep postProcessBeanDefRegistry = applicationStartup.start("spring.context.beandef-registry.post-process")
					.tag("postProcessor", postProcessor::toString);
			// 配置类的后置处理器，会再次解析配置类。「ConfigurationClassPostProcessor：配置文件解析器」
			postProcessor.postProcessBeanDefinitionRegistry(registry);
			postProcessBeanDefRegistry.end();
		}
	}

	/**
	 * Invoke the given BeanFactoryPostProcessor beans.
	 */
	private static void invokeBeanFactoryPostProcessors(
			Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			StartupStep postProcessBeanFactory = beanFactory.getApplicationStartup().start("spring.context.bean-factory.post-process")
					.tag("postProcessor", postProcessor::toString);
			postProcessor.postProcessBeanFactory(beanFactory);
			postProcessBeanFactory.end();
		}
	}

	/**
	 * Register the given BeanPostProcessor beans.
	 */
	private static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {

		if (beanFactory instanceof AbstractBeanFactory) {
			// Bulk addition is more efficient against our CopyOnWriteArrayList there
			((AbstractBeanFactory) beanFactory).addBeanPostProcessors(postProcessors);
		}
		else {
			for (BeanPostProcessor postProcessor : postProcessors) {
				beanFactory.addBeanPostProcessor(postProcessor);
			}
		}
	}


	/**
	 * BeanPostProcessor that logs an info message when a bean is created during
	 * BeanPostProcessor instantiation, i.e. when a bean is not eligible for
	 * getting processed by all BeanPostProcessors.
	 */
	private static final class BeanPostProcessorChecker implements BeanPostProcessor {

		private static final Log logger = LogFactory.getLog(BeanPostProcessorChecker.class);

		private final ConfigurableListableBeanFactory beanFactory;

		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory, int beanPostProcessorTargetCount) {
			this.beanFactory = beanFactory;
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (!(bean instanceof BeanPostProcessor) && !isInfrastructureBean(beanName) &&
					this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
				if (logger.isInfoEnabled()) {
					logger.info("Bean '" + beanName + "' of type [" + bean.getClass().getName() +
							"] is not eligible for getting processed by all BeanPostProcessors " +
							"(for example: not eligible for auto-proxying)");
				}
			}
			return bean;
		}

		private boolean isInfrastructureBean(@Nullable String beanName) {
			if (beanName != null && this.beanFactory.containsBeanDefinition(beanName)) {
				BeanDefinition bd = this.beanFactory.getBeanDefinition(beanName);
				return (bd.getRole() == RootBeanDefinition.ROLE_INFRASTRUCTURE);
			}
			return false;
		}
	}

}
