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

	// 执行工厂的后置处理
	public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		// ※※※※※※※※※※※※※※※※※※※※ 调用 BeanDefinitionRegistryPostProcessor 的后置处理器  Begin ※※※※※※※※※※※※※※※※※※※※

		/**
		 * 用于存储"已处理"的后置处理器的集合，防止处理器被重复执行
		 */
		Set<String> processedBeans = new HashSet<>();

		/**
		 * 判断 BeanFactory 是否是 BeanDefinitionRegistry 接口的实现类
		 * beanFactory 类型为 DefaultListableBeanFactory，其实现了 BeanDefinitionRegistry 接口
		 */
		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

			// 用来存储普通的 BeanFactoryPostProcessor 工厂后置处理器
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
			// 用来存储 BeanDefinitionRegistryPostProcessor 类型的 bean 工厂后置处理器
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

			/**
			 * 遍历入参中的后置处理器集合（※※※※※※※※ 默认为空，只有手动添加才非空 ※※※※※※※※）
			 */
			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				/**
				 * 判断 postProcessor 是不是 BeanDefinitionRegistryPostProcessor，
				 * 因为 BeanDefinitionRegistryPostProcessor 扩展了 BeanFactoryPOSTProcessor，
				 * 若返回 true，则直接执行 postProcessBeanDefinitionRegistry 方法，然后把对象装到 registryProcessor 中
				 */
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					/**
					 * 后置处理器类型转换，处理 BeanDefinitionRegistryPostProcessor 类型的工厂后置处理器
					 */
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





			// 临时集合，用于保存当前需要执行的 BeanDefinitionRegistryPostProcessor（每处理完一批，会阶段性清除一批）
			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			/**
			 * 首先处理实现了 BeanDefinitionRegistryPostProcessor 接口的工厂后置处理器，按照其实现的优先级接口排序
			 * 1.PriorityOrdered 接口优先级最高
			 * 2.Order 接口按照其 getOrder() 方法的返回值，值越小，优先级越高
			 * 3.没有实现优先级接口，按照其配置(加载)的顺序执行
			 *
			 * 先从容器中获取所有的实现了 PriorityOrdered 接口的工厂后置处理器（BeanDefinitionRegistryPostProcessor）的 beanName
			 * 例如：配置类后置处理器 internalConfigurationAnnotationProcessor，类型是 ConfigurationClassPostProcessor
			 *
			 * 获得实现了 BeanDefinitionRegistryPostProcessor 接口的实现类的名称，封装进数组 postProcessorNames 中，
			 * 一般情况下，只会找到一个，即配置类后置处理器 ConfigurationClassPostProcessor
			 * 此处有一个坑，为什么自己创建了一个实现了 BeanDefinitionRegistryPostProcessor 接口的类，也使用了 @Component 注解
			 * 但在这里却无法获得？
			 * 因为直到这一步，Spring 还没有去扫描，扫描是在 ConfigurationClassPostProcessor 类中完成的，
			 * 也就是下面的第一个 invokeBeanDefinitionRegistryPostProcessors() 方法
			 */
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					/**
					 * 从容器中获得这个后置处理器「getBean 创建对象」，并放入这个临时集合中。
					 * beanFactory.getBean()：第一次获取时会创建这个后置处理器，
					 * 例如配置类后置处理器 ConfigurationClassPostProcessor 就在此创建了对象
					 *
					 * ConfigurationClassPostProcessor 内部可以执行 扫描 Bean、Import、ImportResource 等操作
					 * 用来处理配置类的各种逻辑（处理分两种情况，传统配置类和普通的 Bean）
					 */
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					// 把 name 同时也加入到"已处理的后置处理器"集合中，后续会根据这个集合来判断此后置处理器是否已经被执行过了
					processedBeans.add(ppName);
				}
			}

			/**
			 * 利用优先级排序
			 * 根据是否实现了 PriorityOrdered 接口、Order 接口，以及 Order 接口的 getOrder() 方法的返回值 进行排序
			 * 1.PriorityOrdered 接口优先级最高
			 * 2.Order 接口的 getOrder() 方法的返回值越小，优先级越高
			 * 3.没有实现优先级接口，则按照配置(加载)的先后顺序
			 */
			sortPostProcessors(currentRegistryProcessors, beanFactory);

			/**
			 * 添加到 registryProcessors 中，方便后续执行方法 postProcessBeanFactory
			 * 即合并后置处理器。为什么要合并，因为集合 registryProcessors 是装载 BeanDefinitionRegistryPostProcessor 的，
			 * 一开始时，Spring 只会执行 BeanDefinitionRegistryPostProcessor 独有的方法，而不会执行其父类的方法，即 BeanPostProcessor 的方法
			 * 所以这里需要把后置处理器放入到一个集合中，后续统一执行父类的方法
			 */
			registryProcessors.addAll(currentRegistryProcessors);

			/**
			 * ※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※
			 * 执行 BeanDefinitionRegistryPostProcessor 中的 postProcessBeanDefinitionRegistry() 方法
			 * 即执行 BeanDefinitionRegistry 的后置处理功能
			 * 典型的就是配置类的后置处理器 ConfigurationClassPostProcessor，
			 * 会解析配置类，进行 bean 定义的加载、包扫描、@Import 导入等操作，将 BeanDefinition 注册进 BeanDefinitionMap 中
			 *
			 * Spring 热插拔的体现，例如 ConfigurationClassPostProcessor 就相当于一个组件，Spring 很多事情就是交给组件去管理
			 * 如果不想用这个组件，直接把注册组件的那一步去掉就可以了。
			 * ※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※
			 */
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());
			// 执行完成后，清空临时集合，准备下一轮
			currentRegistryProcessors.clear();

			/**
			 * 再次获取 BeanDefinitionRegistryPostProcessor 工厂后置处理器的 beanName，
			 * 再次获取的原因是，上述步骤有可能会产生新的 BeanDefinitionRegistryPostProcessor 后置处理器，因此再次获取一次，以防漏掉
			 * 然后根据是否实现了 Order 接口，再进行筛选
			 */
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				/**
				 * 若在上述步骤中已经处理过的后置处理器，这里不再处理
				 */
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
			 * 最后，没有实现任何排序及优先级接口的情况，再次获取 BeanDefinitionRegistryPostProcessor 工厂后置处理器的 beanName
			 * 以防漏掉上述步骤产生新的 BeanDefinitionRegistryPostProcessor 后置处理器
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
			 * 即执行其父类（BeanFactoryPostProcessor）的 postProcessBeanFactory 方法。
			 * registryProcessors 中存储 BeanDefinitionRegistryPostProcessor 类型的 BeanFactoryPostProcessor 工厂后置处理器
			 * 配置类的后置处理器 ConfigurationClassPostProcessor 会再次执行后置处理方法，
			 * 主要是修改了标注了 @Configuration 注解的配置类的 BeanDefinition 的 beanClass 属性为 cglib 动态代理类型，
			 * 在创建"全"配置类时，创建动态代理类
			 */
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);

			/**
			 * 统一调用普通类型的工厂后置处理器的 postProcessBeanFactory 方法，
			 * regularPostProcessors 中存储普通的 BeanFactoryPostProcessor 工厂后置处理器，默认为空
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

		// ※※※※※※※※※※※※※※※※※※※※ 调用 BeanDefinitionRegistryPostProcessor 的后置处理器  End ※※※※※※※※※※※※※※※※※※※※









		/**
		 * 以上环节，参数 beanFactoryPostProcessors，以及工厂中所有类型为 BeanDefinitionRegistryPostProcessor 的 bean 就已经全部都处理完成了。
		 * 接下来处理工厂中只实现了接口 BeanFactoryPostProcessor 的 bean
		 */








		// ※※※※※※※※※※※※※※※※※※※※ 调用 BeanFactoryPostProcessor 的后置处理器  Begin ※※※※※※※※※※※※※※※※※※※※

		// 从工厂中获取所有实现 BeanFactoryPostProcessor 接口的实现类的 beanName
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// 存储实现了优先级（PriorityOrdered）接口的 BeanFactoryPostProcessor 的实现类
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		// 存储实现了排序接口（Ordered）接口的 BeanFactoryPostProcessor 的实现类
		List<String> orderedPostProcessorNames = new ArrayList<>();
		// 存放无序的（即未实现 PriorityOrdered 接口和 Ordered 接口）的 BeanFactoryPostProcessor 的实现类名称
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();

		// 循环处理
		for (String ppName : postProcessorNames) {
			if (processedBeans.contains(ppName)) {
				/**
				 * 直接空实现，因为 processedBeans 中，已经记录了前面处理过的 BeanDefinitionRegistryPostProcessor 的 bean 的名称
				 * 表示已经处理过了，不再重复处理
				 * skip - already processed in first phase above
				 */
			}

			else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				// 如果实现了 PriorityOrdered 接口，加入到 priorityOrderedPostProcessors 中
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}

			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				//如果实现了 Ordered 接口，加入到 orderedPostProcessorNames 中
				orderedPostProcessorNames.add(ppName);
			}

			else {
				// 如果既没有实现 PriorityOrdered 接口，也没有实现 Ordered 接口，则加入到 nonOrderedPostProcessorNames 中
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		/**
		 * 利用优先级排序（实现了 PriorityOrdered 接口）
		 */
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		// 执行工厂后置处理器中的 postProcessBeanFactory 方法
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// 接下来，从工厂中获取所有的实现了 Ordered 接口的 BeanFactoryPostProcessor
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}

		// 根据 Ordered 接口排序
		sortPostProcessors(orderedPostProcessors, beanFactory);
		// 执行工厂后置处理器的 postProcessBeanFactory 方法
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// 最后，没有实现任何优先级或排序接口的情况，从容器中拿到所有 BeanFactoryPostProcessor
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}

		/**
		 * 执行工厂后置处理器的 postProcessBeanFactory 方法
		 * 例如：EventListenerMethodProcesson.postProcessBeanFactory()，只是拿到容器中所有的事件监听工厂(EventListenerFactory)进行排序
		 */
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

		// 清除元数据相关的缓存，后置处理器可能已经修改了原始的一些元数据
		beanFactory.clearMetadataCache();

		// ※※※※※※※※※※※※※※※※※※※※ 调用 BeanFactoryPostProcessor 的后置处理器  end ※※※※※※※※※※※※※※※※※※※※
	}


	/**
	 * 注册所有的后置处理器，并按照以下几种方式分类，并重进行新排序和注册进工厂；
	 * 最后将 ApplicationListenerDetector 放到后置处理器容器的最后一位。
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
		// 计算容器中 bean 后置处理器的数量（已经存在 + 1 + 容器中还没有注册的）
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		// 检测哪些 bean 没有资格被 bean 后置处理器所处理，记录相应的日志信息
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		/**
		 * 按照 BeanPostProcessor 实现的优先级接口来分离后置处理器
		 */
		// Separate between BeanPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		// 实现了优先级接口(PriorityOrdered)的后置处理器集合
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		// 存放 Spring 内部的 bean 后置处理器集合
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
		// 实现了排序接口(Ordered)的后置处理器名称集合
		List<String> orderedPostProcessorNames = new ArrayList<>();
		// 存放无序的(即没有实现优先级或者排序接口的)后置处理器的名称的集合
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();

		// 遍历所有 bean 后置处理器的名称
		for (String ppName : postProcessorNames) {
			// 根据后置处理器名字，分类型进行拆分集合。（匹配实现了优先级接口的后置处理器）
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				/**
				 * 调用 getBean 流程创建 bean 的后置处理器
				 * 从容器中获取实现了优先级接口的后置处理器对象。
				 * 例如：AutowiredAnnotationBeanPostProcessor
				 */
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				// 添加到优先级接口集合
				priorityOrderedPostProcessors.add(pp);
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					/**
					 * 筛选出 MergedBeanDefinitionPostProcessor 类型的后置处理器。
					 * 与实例化注解关系密切，如与注解 @Autowired 相关的 AutowiredAnnotationBeanPostProcessor
					 */
					internalPostProcessors.add(pp);
				}
			}
			// 匹配实现了排序接口的后置处理器
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				/**
				 * 筛选出所有实现了排序接口(Ordered)的后置处理器名称
				 * 例如：internalAutoProxyCreator，即 AOP 的后置处理器 AnnotationAwareAspectJAutoProxyCreator
				 */
				orderedPostProcessorNames.add(ppName);
			}
			else {
				// 筛选出所有无序（即没有实现优先级接口和排序接口的）的后置处理器名字
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		/**
		 * 首先对实现了优先级接口(PriorityOrdered)的后置处理器集合进行排序。
		 * First, register the BeanPostProcessors that implement PriorityOrdered.
		 */
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		// 将实现了优先级接口(PriorityOrdered)的后置处理器集合注册进工厂
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		/**
		 * 获取所有实现了排序接口(Ordered)的后置处理器，例如 AOP 代理功能入口的 AnnotationAwareAspectJAutoProxyCreator。
		 * Next, register the BeanPostProcessors that implement Ordered.
		 */
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String ppName : orderedPostProcessorNames) {
			/**
			 * 调用 getBean 流程创建 bean 的后置处理器的对象
			 * 从容器中获取实现了 Ordered 接口的后置处理器对象
			 * 例如：AOP 的后置处理器 AnnotationAwareAspectJAutoProxyCreator 在此创建了
			 */
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

		/**
		 * 获取所有没有实现优先级接口(PriorityOrdered)和没有实现排序接口(Ordered)的后置处理器。
		 * Now, register all regular BeanPostProcessors.
		 */
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String ppName : nonOrderedPostProcessorNames) {
			/**
			 * 调用 getBean 流程创建 bean 的后置处理器
			 * 从容器中获取没有实现优先级接口(PriorityOrdered)和没有实现排序接口(Ordered)的后置处理器对象。（创建对象）
			 */
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}

		// 将所有没有实现优先级接口(PriorityOrdered)和没有实现排序接口(Ordered)的后置处理器注册进工厂
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		/**
		 * 将所有 MergedBeanDefinitionPostProcessor 类型的后置处理器集合进行排序。
		 * Finally, re-register all internal BeanPostProcessors.
		 */
		sortPostProcessors(internalPostProcessors, beanFactory);
		// 将所有 MergedBeanDefinitionPostProcessor 类型的后置处理器注册进工厂
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		/**
		 * 重新注册后置处理器 ApplicationListenerDetector，把他放到后置处理器容器的最后一位
		 * Re-register post-processor for detecting inner beans as ApplicationListeners,
		 * moving it to the end of the processor chain (for picking up proxies etc).
		 */
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
			/**
			 * 配置类的后置处理器，会在此解析配置类。「ConfigurationClassPostProcessor：配置文件解析器」
			 * 将 BeanDefinition 注册进 BeanDefinitionMap 中
			 */
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
