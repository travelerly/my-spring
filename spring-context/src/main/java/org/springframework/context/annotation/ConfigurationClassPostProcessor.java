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

package org.springframework.context.annotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.parsing.FailFastProblemReporter;
import org.springframework.beans.factory.parsing.PassThroughSourceExtractor;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ApplicationStartupAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ConfigurationClassEnhancer.EnhancedConfiguration;
import org.springframework.core.NativeDetector;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.StartupStep;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * {@link BeanFactoryPostProcessor} used for bootstrapping processing of
 * {@link Configuration @Configuration} classes.
 *
 * <p>Registered by default when using {@code <context:annotation-config/>} or
 * {@code <context:component-scan/>}. Otherwise, may be declared manually as
 * with any other {@link BeanFactoryPostProcessor}.
 *
 * <p>This post processor is priority-ordered as it is important that any
 * {@link Bean @Bean} methods declared in {@code @Configuration} classes have
 * their corresponding bean definitions registered before any other
 * {@code BeanFactoryPostProcessor} executes.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 3.0
 */

/**
 * 该类会解析标注了注解 @Configuration 的配置类，
 * 还会解析标注了注解 @ComponentScan、@ComponentScans 注解扫描的包，
 * 以及解析 @Import 等注解
 */
public class ConfigurationClassPostProcessor implements BeanDefinitionRegistryPostProcessor,
		PriorityOrdered, ResourceLoaderAware, ApplicationStartupAware, BeanClassLoaderAware, EnvironmentAware {

	/**
	 * A {@code BeanNameGenerator} using fully qualified class names as default bean names.
	 * <p>This default for configuration-level import purposes may be overridden through
	 * {@link #setBeanNameGenerator}. Note that the default for component scanning purposes
	 * is a plain {@link AnnotationBeanNameGenerator#INSTANCE}, unless overridden through
	 * {@link #setBeanNameGenerator} with a unified user-level bean name generator.
	 * @since 5.2
	 * @see #setBeanNameGenerator
	 */
	public static final AnnotationBeanNameGenerator IMPORT_BEAN_NAME_GENERATOR =
			FullyQualifiedAnnotationBeanNameGenerator.INSTANCE;

	private static final String IMPORT_REGISTRY_BEAN_NAME =
			ConfigurationClassPostProcessor.class.getName() + ".importRegistry";


	private final Log logger = LogFactory.getLog(getClass());

	private SourceExtractor sourceExtractor = new PassThroughSourceExtractor();

	private ProblemReporter problemReporter = new FailFastProblemReporter();

	@Nullable
	private Environment environment;

	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	@Nullable
	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();

	private boolean setMetadataReaderFactoryCalled = false;

	private final Set<Integer> registriesPostProcessed = new HashSet<>();

	private final Set<Integer> factoriesPostProcessed = new HashSet<>();

	@Nullable
	private ConfigurationClassBeanDefinitionReader reader;

	private boolean localBeanNameGeneratorSet = false;

	/* Using short class names as default bean names by default. */
	private BeanNameGenerator componentScanBeanNameGenerator = AnnotationBeanNameGenerator.INSTANCE;

	/* Using fully qualified class names as default bean names by default. */
	private BeanNameGenerator importBeanNameGenerator = IMPORT_BEAN_NAME_GENERATOR;

	private ApplicationStartup applicationStartup = ApplicationStartup.DEFAULT;


	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;  // within PriorityOrdered
	}

	/**
	 * Set the {@link SourceExtractor} to use for generated bean definitions
	 * that correspond to {@link Bean} factory methods.
	 */
	public void setSourceExtractor(@Nullable SourceExtractor sourceExtractor) {
		this.sourceExtractor = (sourceExtractor != null ? sourceExtractor : new PassThroughSourceExtractor());
	}

	/**
	 * Set the {@link ProblemReporter} to use.
	 * <p>Used to register any problems detected with {@link Configuration} or {@link Bean}
	 * declarations. For instance, an @Bean method marked as {@code final} is illegal
	 * and would be reported as a problem. Defaults to {@link FailFastProblemReporter}.
	 */
	public void setProblemReporter(@Nullable ProblemReporter problemReporter) {
		this.problemReporter = (problemReporter != null ? problemReporter : new FailFastProblemReporter());
	}

	/**
	 * Set the {@link MetadataReaderFactory} to use.
	 * <p>Default is a {@link CachingMetadataReaderFactory} for the specified
	 * {@linkplain #setBeanClassLoader bean class loader}.
	 */
	public void setMetadataReaderFactory(MetadataReaderFactory metadataReaderFactory) {
		Assert.notNull(metadataReaderFactory, "MetadataReaderFactory must not be null");
		this.metadataReaderFactory = metadataReaderFactory;
		this.setMetadataReaderFactoryCalled = true;
	}

	/**
	 * Set the {@link BeanNameGenerator} to be used when triggering component scanning
	 * from {@link Configuration} classes and when registering {@link Import}'ed
	 * configuration classes. The default is a standard {@link AnnotationBeanNameGenerator}
	 * for scanned components (compatible with the default in {@link ClassPathBeanDefinitionScanner})
	 * and a variant thereof for imported configuration classes (using unique fully-qualified
	 * class names instead of standard component overriding).
	 * <p>Note that this strategy does <em>not</em> apply to {@link Bean} methods.
	 * <p>This setter is typically only appropriate when configuring the post-processor as a
	 * standalone bean definition in XML, e.g. not using the dedicated {@code AnnotationConfig*}
	 * application contexts or the {@code <context:annotation-config>} element. Any bean name
	 * generator specified against the application context will take precedence over any set here.
	 * @since 3.1.1
	 * @see AnnotationConfigApplicationContext#setBeanNameGenerator(BeanNameGenerator)
	 * @see AnnotationConfigUtils#CONFIGURATION_BEAN_NAME_GENERATOR
	 */
	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		Assert.notNull(beanNameGenerator, "BeanNameGenerator must not be null");
		this.localBeanNameGeneratorSet = true;
		this.componentScanBeanNameGenerator = beanNameGenerator;
		this.importBeanNameGenerator = beanNameGenerator;
	}

	@Override
	public void setEnvironment(Environment environment) {
		Assert.notNull(environment, "Environment must not be null");
		this.environment = environment;
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		Assert.notNull(resourceLoader, "ResourceLoader must not be null");
		this.resourceLoader = resourceLoader;
		if (!this.setMetadataReaderFactoryCalled) {
			this.metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);
		}
	}

	@Override
	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
		if (!this.setMetadataReaderFactoryCalled) {
			this.metadataReaderFactory = new CachingMetadataReaderFactory(beanClassLoader);
		}
	}

	@Override
	public void setApplicationStartup(ApplicationStartup applicationStartup) {
		this.applicationStartup = applicationStartup;
	}

	/**
	 * 把配置类中所有的 bean 定义信息导入进来。Derive further bean definitions from the configuration classes in the registry.
	 */
	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
		// 获取注册中心 registry 的一个唯一 hash 值，用于去重判断
		int registryId = System.identityHashCode(registry);

		// 判断 postProcessBeanDefinitionRegistry() 方法是否已经被调用过了
		if (this.registriesPostProcessed.contains(registryId)) {
			throw new IllegalStateException(
					"postProcessBeanDefinitionRegistry already called on this post-processor against " + registry);
		}

		// 判断 postProcessBeanFactory() 方法是否已经被调用过了
		if (this.factoriesPostProcessed.contains(registryId)) {
			throw new IllegalStateException(
					"postProcessBeanFactory already called on this post-processor against " + registry);
		}

		// registry 第一次调用 processConfigBeanDefinitions() 方法，记录 registry 的唯一 hash 值
		this.registriesPostProcessed.add(registryId);

		/**
		 * 解析配置类：处理配置类中配置的 BeanDefinition 信息
		 * 将配置类中配置的 BeanDefinition 信息注册进 BeanDefinitionMap 中
		 */
		processConfigBeanDefinitions(registry);
	}

	/**
	 * Prepare the Configuration classes for servicing bean requests at runtime
	 * by replacing them with CGLIB-enhanced subclasses.
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		// 获取 beanFactory 的 hash 值，用于去重判断
		int factoryId = System.identityHashCode(beanFactory);
		// 判断当前方法（postProcessBeanFactory）是否已经被执行过了。
		if (this.factoriesPostProcessed.contains(factoryId)) {
			throw new IllegalStateException(
					"postProcessBeanFactory already called on this post-processor against " + beanFactory);
		}

		// 至此，说明当前方法首次被调用执行，标记当前方法已被执行
		this.factoriesPostProcessed.add(factoryId);
		if (!this.registriesPostProcessed.contains(factoryId)) {
			// BeanDefinitionRegistryPostProcessor hook apparently not supported...
			// Simply call processConfigurationClasses lazily at this point then.
			processConfigBeanDefinitions((BeanDefinitionRegistry) beanFactory);
		}

		/**
		 * 对容器中的配置类进行增强处理，仅对标注 @Configuration 注解的配置类进行增强
		 * 修改"全"配置类的 BeanDefinition 的 beanClass 属性为 cglib 的动态代理类型
		 */
		enhanceConfigurationClasses(beanFactory);
		// 注册 bean 后置处理器 ImportAwareBeanPostProcessor
		beanFactory.addBeanPostProcessor(new ImportAwareBeanPostProcessor(beanFactory));
	}

	/**
	 * Build and validate a configuration model based on the registry of
	 * {@link Configuration} classes.
	 */
	public void processConfigBeanDefinitions(BeanDefinitionRegistry registry) {

		// 用于存放容器中的所有配置类的 BeanDefinition，并将其封装成 BeanDefinitionHolder
		List<BeanDefinitionHolder> configCandidates = new ArrayList<>();

		// 获取容器中所有 BeanDefinition 的名字。「包含后置处理器和配置类」
		String[] candidateNames = registry.getBeanDefinitionNames();

		// 遍历处理，筛选出所有的配置类的 BeanDefinition，并封装成 BeanDefinitionHolder，再添加到结合 configCandidates 中。
		for (String beanName : candidateNames) {

			// 根据名字获取 BeanDefinition
			BeanDefinition beanDef = registry.getBeanDefinition(beanName);

			/**
			 * 判断当前遍历到的配置类对象是否被解析过
			 * 判断当前 BeanDefinition 是否存在属性名称为 ConfigurationClassPostProcessor.configurationClass 的值
			 * 如果不存在，意味着配置类的 BeanDefinition 并没有被处理过。
			 * 第一次执行该方法时，默认不存在。
			 *
			 * 内部有两个标记来标记是否已经处理过了
			 * 注册配置类的时候，可以不添加 @Configuration 注解，而是直接使用 @Component、@ComponentScan、@Import、@ImportResource 等注解
			 * 而添加了注解 @Configuration 的配置了称为 "FULL" 配置类，添加了其它注解的配置类则称为 "Lite" 配置类
			 * 注册的 "FULL" 配置类，在 getBean() 时，获取的是配置类的 cglib 代理的类
			 * 注册的 "Lite" 配置类，在 getBean() 时，获取的是原本的那个配置类
			 *
			 * 对于 "FULL" 配置类和 "Lite" 配置类的对比，记录在 ConfCglibConfig 中
			 */
			if (beanDef.getAttribute(ConfigurationClassUtils.CONFIGURATION_CLASS_ATTRIBUTE) != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Bean definition has already been processed as a configuration class: " + beanDef);
				}
			}

			/**
			 * 判断当前遍历到的 BeanDefinition 是否为标注了 @Configuration 注解，即是否为(完整)配置类
			 */
			else if (ConfigurationClassUtils.checkConfigurationClassCandidate(beanDef, this.metadataReaderFactory)) {
				/**
				 * 如果当前 BeanDefinition 是配置类，就将 BeanDefinition 封装到 BeanDefinitionHolder 中，
				 * 同时将 BeanDefinitionHolder 添加到集合 configCandidates 中
				 */
				configCandidates.add(new BeanDefinitionHolder(beanDef, beanName));
			}
		}

		// Return immediately if no @Configuration classes were found
		if (configCandidates.isEmpty()) {
			// 如果没有配置类，就直接返回
			return;
		}

		// Sort by previously determined @Order value, if applicable
		// 如果 BeanDefinition 对应的类上标注了 @Order 注解，则对其进行排序，属性值越小优先级越高
		configCandidates.sort((bd1, bd2) -> {
			int i1 = ConfigurationClassUtils.getOrder(bd1.getBeanDefinition());
			int i2 = ConfigurationClassUtils.getOrder(bd2.getBeanDefinition());
			return Integer.compare(i1, i2);
		});

		/**
		 * 检测容器中是否注册了自定义的 bean 名称生成策略组件
		 * Detect any custom bean name generation strategy supplied through the enclosing application context
		 */
		SingletonBeanRegistry sbr = null;
		if (registry instanceof SingletonBeanRegistry) {
			sbr = (SingletonBeanRegistry) registry;
			if (!this.localBeanNameGeneratorSet) {
				// bean 名称生成策略的一个组件（默认为空）
				BeanNameGenerator generator = (BeanNameGenerator) sbr.getSingleton(
						AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR);
				if (generator != null) {
					// 设置由 @ComponentScan 注解导入进来的 bean 的名称生成器(默认首字母小写)，也可以自定义
					this.componentScanBeanNameGenerator = generator;
					// 设置由 @Import 注解导入进来的 bean 的名称生成器(首字母小写)，也可以自定义
					this.importBeanNameGenerator = generator;
				}
			}
		}

		if (this.environment == null) {
			// 初始化环境变量
			this.environment = new StandardEnvironment();
		}

		/**
		 * 创建一个配置类解析器对象
		 * ConfigurationClassParser：用于解析标注了 @Configuration 注解的配置类的解析器。
		 * Parse each @Configuration class
		 */
		ConfigurationClassParser parser = new ConfigurationClassParser(
				this.metadataReaderFactory, this.problemReporter, this.environment,
				this.resourceLoader, this.componentScanBeanNameGenerator, registry);

		// 将上面处理过的配置类存入这个 set 集合中
		Set<BeanDefinitionHolder> candidates = new LinkedHashSet<>(configCandidates);
		// 存放已经解析完毕的配置类(BeanDefinitionHolder 解析完毕后，会封装在 ConfigurationClass 中)
		Set<ConfigurationClass> alreadyParsed = new HashSet<>(configCandidates.size());

		// 循环解析配置类的 BeanDefinition
		do {
			StartupStep processConfig = this.applicationStartup.start("spring.context.config-classes.parse");

			/**
			 * 解析配置类
			 * candidates：上面处理过的配置类
			 */
			parser.parse(candidates);
			parser.validate();

			// 临时存放 candidates 解析完毕后，封装得到的 ConfigurationClass 集合
			Set<ConfigurationClass> configClasses = new LinkedHashSet<>(parser.getConfigurationClasses());
			// 剔除上一轮 while 循环中，已经全部解析处理完毕的元素
			configClasses.removeAll(alreadyParsed);

			/**
			 * 为每一个组件处理 @Import 注解的配置类信息。
			 * Read the model and create bean definitions based on its content
			 */
			if (this.reader == null) {
				this.reader = new ConfigurationClassBeanDefinitionReader(
						registry, this.sourceExtractor, this.resourceLoader, this.environment,
						this.importBeanNameGenerator, parser.getImportRegistry());
			}

			/**
			 * 此处才把 @Bean、@Import、@ImportResource 导入的组件注册到 BeanDefinitionMap 中
			 * configClasses 中保存了解析过的 BeanDefinition 数据
			 */
			this.reader.loadBeanDefinitions(configClasses);
			// 将解析完毕的 configClasses 添加到集合 alreadyParsed 中
			alreadyParsed.addAll(configClasses);
			processConfig.tag("classCount", () -> String.valueOf(configClasses.size())).end();

			candidates.clear();

			if (registry.getBeanDefinitionCount() > candidateNames.length) {

				// 至此说明容器中的 BeanDefinition 的数量已经大于之前容器中的数量了，例如上步注册了注解 @Bean 的方法封装了的 BeanDefinition

				// 获取容器中已经注册的 bean 的名称集合
				String[] newCandidateNames = registry.getBeanDefinitionNames();
				// 存储方法执行前，容器中的 bean 的名称集合
				Set<String> oldCandidateNames = new HashSet<>(Arrays.asList(candidateNames));
				Set<String> alreadyParsedClasses = new HashSet<>();

				// 循环 alreadyParsed，把类名加入到 alreadyParsedClasses
				for (ConfigurationClass configurationClass : alreadyParsed) {
					// 存储解析完毕了的配置类对应的 configurationClass
					alreadyParsedClasses.add(configurationClass.getMetadata().getClassName());
				}
				for (String candidateName : newCandidateNames) {
					/**
					 * 注册解析配置类过程中，新注册的那些 bean 的名称
					 * 例如从添加了注解 @Bean 上解析出来的 BeanDefinition
					 */
					if (!oldCandidateNames.contains(candidateName)) {
						BeanDefinition bd = registry.getBeanDefinition(candidateName);
						if (ConfigurationClassUtils.checkConfigurationClassCandidate(bd, this.metadataReaderFactory) &&
								!alreadyParsedClasses.contains(bd.getBeanClassName())) {
							/**
							 * 将这些解析过程中，新添加的 BeanDefinition 作为下一轮 while 循环中处理的候选类
							 * 这些 @Bean 方法得到的类，一般都不可能是配置类
							 */
							candidates.add(new BeanDefinitionHolder(bd, candidateName));
						}
					}
				}
				candidateNames = newCandidateNames;
			}
		}
		while (!candidates.isEmpty());


		/**
		 * 处理 @ImportRegistry 注解的配置类
		 * 解析名称为 ConfigurationClassPostProcessor 的单例对象
		 * Register the ImportRegistry as a bean in order to support ImportAware @Configuration classes
		 */
		if (sbr != null && !sbr.containsSingleton(IMPORT_REGISTRY_BEAN_NAME)) {
			// 从配置类解析器 parser 中获取该单例对象，并注册到容器中
			sbr.registerSingleton(IMPORT_REGISTRY_BEAN_NAME, parser.getImportRegistry());
		}

		if (this.metadataReaderFactory instanceof CachingMetadataReaderFactory) {
			// Clear cache in externally provided MetadataReaderFactory; this is a no-op
			// for a shared cache since it'll be cleared by the ApplicationContext.
			((CachingMetadataReaderFactory) this.metadataReaderFactory).clearCache();
		}
	}

	/**
	 * Post-processes a BeanFactory in search of Configuration class BeanDefinitions;
	 * any candidates are then enhanced by a {@link ConfigurationClassEnhancer}.
	 * Candidate status is determined by BeanDefinition attribute metadata.
	 * @see ConfigurationClassEnhancer
	 */
	public void enhanceConfigurationClasses(ConfigurableListableBeanFactory beanFactory) {
		StartupStep enhanceConfigClasses = this.applicationStartup.start("spring.context.config-classes.enhance");
		Map<String, AbstractBeanDefinition> configBeanDefs = new LinkedHashMap<>();
		// 遍历容器中已经注册的每个组件名称
		for (String beanName : beanFactory.getBeanDefinitionNames()) {
			// 根据组件名称，获取组件定义信息
			BeanDefinition beanDef = beanFactory.getBeanDefinition(beanName);
			// 获取 ConfigurationClassPostProcessor.configurationClass 属性值
			Object configClassAttr = beanDef.getAttribute(ConfigurationClassUtils.CONFIGURATION_CLASS_ATTRIBUTE);
			AnnotationMetadata annotationMetadata = null;
			MethodMetadata methodMetadata = null;
			if (beanDef instanceof AnnotatedBeanDefinition) {
				AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDef;
				annotationMetadata = annotatedBeanDefinition.getMetadata();
				// 获取工厂方法的注解元数据
				methodMetadata = annotatedBeanDefinition.getFactoryMethodMetadata();
			}

			/**
			 * 如果属性值 configClassAttr 存在，或者组件定义信息中设置了工厂方法的注解，
			 * 并且组件是 AbstractBeanDefinition 类型的组件
			 */
			if ((configClassAttr != null || methodMetadata != null) && beanDef instanceof AbstractBeanDefinition) {
				// Configuration class (full or lite) or a configuration-derived @Bean method
				// -> eagerly resolve bean class at this point, unless it's a 'lite' configuration
				// or component class without @Bean methods.
				AbstractBeanDefinition abd = (AbstractBeanDefinition) beanDef;
				if (!abd.hasBeanClass()) {
					boolean liteConfigurationCandidateWithoutBeanMethods =
							(ConfigurationClassUtils.CONFIGURATION_CLASS_LITE.equals(configClassAttr) &&
								annotationMetadata != null && !ConfigurationClassUtils.hasBeanMethods(annotationMetadata));
					if (!liteConfigurationCandidateWithoutBeanMethods) {
						try {
							abd.resolveBeanClass(this.beanClassLoader);
						}
						catch (Throwable ex) {
							throw new IllegalStateException(
									"Cannot load configuration class: " + beanDef.getBeanClassName(), ex);
						}
					}
				}
			}

			/**
			 * 只有"full"版配置类才会创建 cglib 动态代理
			 * 虽然在指定配置的时候，可以不标注注解 @Configuration，但其与标注了注解 @Configuration 的区别在于：
			 * 配置类中一个 @Bean 的使用方法时引用另一个 Bean 的时候，
			 * 1.如果没有标注注解 @Configuration，就会重复加载 Bean
			 * 2.如果标注了注解 @Configuration，会在这里创建 cglib 代理，当调用 @Bean 方法时会先检测容器中是否存在这个 Bean，就不会重复加载 Bean
			 */
			if (ConfigurationClassUtils.CONFIGURATION_CLASS_FULL.equals(configClassAttr)) {
				// 组件定义信息必须是 AbstractBeanDefinition 类型，才能对其进行增强
				if (!(beanDef instanceof AbstractBeanDefinition)) {
					throw new BeanDefinitionStoreException("Cannot enhance @Configuration bean definition '" +
							beanName + "' since it is not stored in an AbstractBeanDefinition subclass");
				}
				else if (logger.isInfoEnabled() && beanFactory.containsSingleton(beanName)) {
					logger.info("Cannot enhance @Configuration bean definition '" + beanName +
							"' since its singleton instance has been created too early. The typical cause " +
							"is a non-static @Bean method with a BeanDefinitionRegistryPostProcessor " +
							"return type: Consider declaring such methods as 'static'.");
				}
				// 将符合增强条件的组件定义信息添加到集合 configBeanDefs 中
				configBeanDefs.put(beanName, (AbstractBeanDefinition) beanDef);
			}
		}
		if (configBeanDefs.isEmpty() || NativeDetector.inNativeImage()) {
			// nothing to enhance -> return immediately
			enhanceConfigClasses.end();
			return;
		}

		// 创建一个配置类增强器
		ConfigurationClassEnhancer enhancer = new ConfigurationClassEnhancer();
		// 遍历增强
		for (Map.Entry<String, AbstractBeanDefinition> entry : configBeanDefs.entrySet()) {
			AbstractBeanDefinition beanDef = entry.getValue();
			// If a @Configuration class gets proxied, always proxy the target class
			beanDef.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
			// Set enhanced subclass of the user-specified bean class
			Class<?> configClass = beanDef.getBeanClass();

			// 对配置类进行动态代理增强，即对这个组件进行 cglib 的动态代理
			Class<?> enhancedClass = enhancer.enhance(configClass, this.beanClassLoader);

			if (configClass != enhancedClass) {
				if (logger.isTraceEnabled()) {
					logger.trace(String.format("Replacing bean definition '%s' existing class '%s' with " +
							"enhanced class '%s'", entry.getKey(), configClass.getName(), enhancedClass.getName()));
				}
				/**
				 * 修改配置类的 BeanDefinition 的 Class 属性，在创建配置类实例的时候会创建 cglib 的动态代理对象
				 * 例如：将 myConfig 的 BeanDefinition 的 beanClass 属性设置为 MyConfig$$EnhancerBySpringCGLIB$$f10858dd
				 */
				beanDef.setBeanClass(enhancedClass);
			}
		}
		enhanceConfigClasses.tag("classCount", () -> String.valueOf(configBeanDefs.keySet().size())).end();
	}


	private static class ImportAwareBeanPostProcessor implements InstantiationAwareBeanPostProcessor {

		private final BeanFactory beanFactory;

		public ImportAwareBeanPostProcessor(BeanFactory beanFactory) {
			this.beanFactory = beanFactory;
		}

		@Override
		public PropertyValues postProcessProperties(@Nullable PropertyValues pvs, Object bean, String beanName) {
			// Inject the BeanFactory before AutowiredAnnotationBeanPostProcessor's
			// postProcessProperties method attempts to autowire other configuration beans.
			if (bean instanceof EnhancedConfiguration) {
				((EnhancedConfiguration) bean).setBeanFactory(this.beanFactory);
			}
			return pvs;
		}

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			if (bean instanceof ImportAware) {
				ImportRegistry ir = this.beanFactory.getBean(IMPORT_REGISTRY_BEAN_NAME, ImportRegistry.class);
				AnnotationMetadata importingClass = ir.getImportingClassFor(ClassUtils.getUserClass(bean).getName());
				if (importingClass != null) {
					((ImportAware) bean).setImportMetadata(importingClass);
				}
			}
			return bean;
		}
	}

}
