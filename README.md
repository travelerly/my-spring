# <img src="src/docs/spring-framework.png" width="80" height="80"> Spring Framework 

---

## Spring 整体架构

#### Spring 如何工作
Spring 暴露给程序员的使用方式是，要么写一个 xml 文件、要么使用注解、要么利用磁盘文件、网络文件等，把需要的功能定义出来，这个信息最终会生成一个组件或者功能配置清单，Spring 会去读取并解析这些功能清单，这些信息就会决定 Spring 框架中的各种行为。

#### Spring 整体架构流程
在 Spring 的底层把所有的资源（xml、注解、网络文件、磁盘文件等）都用 Resource 来表示，Spring 使用 ResourceLoader（资源加载器）加载这些资源，交给 BeanDefinitionReader 来读取和解析，并存放到 Spring 工厂的 BeanDefinitionRegistry （Bean  定义信息注册中心）中，即 Spring 一启动，就将所有资源解析成 BeanDefinition 存入到 BeanDefinitionRegistry 中。（实际是保存在一个 map 中，BeanDefinitionMap），然后 Spring 将这些 bean 的定义信息挨个创建成对象，并存入到 IOC 容器中，Spring 中使用各种池来存储对象，其中单例对象池用于保存所有的单例对象，在使用对象时，就去单例池中获取对象。

```text
ApplicationContext 和 BeanFactory 的作用：
1.BeanFactory 定义工厂创建和获取 Bean 流程的
2.ApplicationContext 定义了 Bean 的增强处理以及容器保存等各种流程
3.ApplicationContext 里面第一次要用到 bean，会使用工厂 BeanFactory 先来创建，创建好后保存在容器中

AOP：
1.AnnotationAspectJAutoProxyCreator 后置处理器，会在启动的时候分析所有标注了 @Aspect 注解的切面信息，将其封装成增强器链，并为目标对象创建代理放在容器中
2.执行期间代理对象会链式调用 AOP 切面定义的增强方法

生命周期：
1.BeanFactoryPostProcessor：在 BeanFactory 初始化前后拦截
2.BeanPostProcessor：在所有组件创建对象及初始化前后拦截
3.InitializingBean：组件单独实现它，可以在组件赋值结束以后调用初始化进行增强处理
4.SmartInitializingBean：所有组件都创建好以后，每个组件再来 SmartInitializingBean

Bean：保存 BeanDefinition 信息→根据 BeanDefinition 信息创建对象→赋值→初始化

Bean的功能增强全都是由 BeanPostProcessor + InitializingBean (合起来)完成的。
使用建议：
1.所有组件可能都会使用的功能，使用后置处理器BeanPostProcessor来实现
2.单组件增强的功能，最好使用生命周期InitializingBean来实现。

新功能分析思路
1.组件的新功能一般都是由Bean的生命周期机制增强出来的
2.这个新功能加入了哪些组件，这些组件在生命周期期间做了什么

Spring的套路点
1.AbstractBeanDefinition 如何给容器中注入了什么组件
2.BeanFactory 初始化完成后，监控其中多了哪些后置处理器
3.分析后置处理器什么时候调用，做了什么功能。
以上所有的前提，是理解容器刷新12大步与getBean流程，防止混乱
a.工厂后置处理器执行
b.bean后置处理器执行、bean的生命周期（后置处理器+InitializingBean）
```
---
#### Spring架构原理图
![](src/docs/spring/Spring架构原理图.jpg)

---
#### 容器刷新完整流程
Spring 容器启动时，先加载一些底层的后置处理器（例如ConfigurationClassPostProcessor「配置类后置处理器」，容器刷新时，执行工厂后置处理器，注册系统内所有配置类定义信息、AutowiredAnnotationBeanPostProcessor「自动装配功能后置处理器」……），然后再将由构造传入的所有主配置类的定义信息注册进容器。然后开始刷新容器的步骤（容器刷新十二大步）。
1. prepareRefresh()：准备上下文环境；
2. obtainFreshBeanFactory()：工厂的创建，BeanFactory第一次创建「有 xml 的解析逻辑」，获取当前准备好的空容器，返回在 this() 环节就准备（new）好的 BeanFactory；
3. prepareBeanFactory(beanFactory)：预准备工厂，给容器中注册了环境信息作为单实例 Bean，方便后续自动装配；beanPostProcessor 池中注册了一些后置处理器（处理监听功能、XXXAware功能）；
4. postProcessBeanFactory(beanFactory)：留给子类的模板方法，允许子类继续对工厂执行一些处理；
5. invokeBeanFactoryPostProcessors(beanFactory)：工厂的增强或修改：执行所有的 BeanFactory 后置处理器，所有的 BeanDefinition 就已经准备就绪了（配置类会在这里解析，注册了所有标有@Component、@ComponentScans、@ImportResource、@PropertySources、@Bean、@Import等注解的 bean）；
6. registerBeanPostProcessors(beanFactory)：注册所有的 bean 的后置处理器。BeanPostProcessor、ApplicationListenerDetector详细参照 Bean 的生命周期流程；
7. initMessageSource()：初始化，国际化组件。观察容器中是否含有MessageResource的定义信息，如果没有就注册一个放到单例池；
8. initApplicationEventMulticaster()：初始化事件多播功能「事件派发」。观察容器中是否有id为applicationEventMulticaster的定义信息，如果没有就注册一个事件多播器ApplicationEventMulticaster放到单例池中；
9. onRefresh()：留给子类继续增强处理逻辑「模板模式」；
10. registerListeners()：注册监听器，关联Spring的事件监听机制。将容器中所有的监听器 ApplicationListener 保存进多播器集合中；
11. finishBeanFactoryInitialization(beanFactory)：bean的创建，完成BeanFactory的初始化。详细参照Bean的初始化流程，再执行所有后初始化操作「SmartInitializingSingleton.afterSingletonsInstantiated」；
12. finishRefresh()：最后的一些清理、事件发送等处理。
![](src/docs/spring/容器刷新完整流程.jpg)

---
#### Bean的初始化流程
![](src/docs/spring/Bean的初始化流程.jpg)

---
#### Bean生命周期
![](src/docs/spring/Bean生命周期.jpg)

---
#### 循环引用
![](src/docs/spring/循环引用.jpg)

---
#### AOP增强流程
![](src/docs/spring/AOP增强流程.jpg)

---
#### AOP执行链执行流程
```
目标方法的执行
容器中保存了组件的代理对象「cglib增强后的对象」，这个对象里面保存了详细信息（比如增强器、目标对象等）

1. CglibAopProxy.intercept(),拦截目标方法的执行
2. 根据ProxyFactory对象获取将要执行的目标方法的拦截器链
   List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
   1. List<Object> interceptorList：保存所有的拦截器，一个默认的ExposeInvocationInterceptor和四个增强器「通知方法」
   2. 遍历所有的增强器，将其转换成 Interceptor：MethodInterceptor[] interceptors = registry.getInterceptors(advisor)
   3. 将增强器转为 List<MethodInterceptor>
      1. 如果是 MethodInterceptor ，直接加入到集合中
      2. 如果不是 MethodInterceptor，则使用 AdvisorAdapter 将增强器转为 MethodInterceptor，转换完成后，返回 MethodInterceptor 数组。
3. 如果没有拦截器链，则直接执行目标方法
   拦截器链：每一个通知方法又被包装成方法拦截器，利用 MethodInterceptor 拦截器机制执行
4. 如果有拦截器链，把需要执行的目标对象、目标方法、拦截器链等信息传入创建的CglibMethodInvokation对象，并调用Object retVal = mi.procceed()
5. 拦截器链的触发过程
   1. 如果没有拦截器或者拦截器索引和拦截器数组-1的大小相同（指定到了最后一个拦截器），则执行目标方法
   2. 链式获取每一个拦截器，拦截器执行 invoke() 方法，每一个拦截器等待下一个拦截器执行完成返回以后再执行
      拦截器链的机制，保证通知方法和目标方法的执行顺序
      
AOP 总结：
1. @EnableAspectJAutoProxy 开启基于注解的 AOP 功能
2. @EnableAspectJAutoProxy 会给容器中注册一个组件 AnnotationAwareAspectJAutoProxyCreator，是一个后置处理器
3. 容器的创建流程
   1. registerBeanPostProcessors(beanFactory) 注册后置处理器，创建 AnnotationAwareAspectJAutoProxyCreator 对象
   2. finishBeanFactoryInitialization(beanFactory) 初始化剩下的单实例 Bean
      1. 创建业务逻辑组件和切面组件
      2. AnnotationAwareAspectJAutoProxyCreator 拦截组件的创建过程
      3. 组件创建完成后，判断组件是否需要增强
         需要增强：将切面的通知方法，包装成增强器（Advisor），给业务逻辑组件创建一个代理对象（反射cglib）
4. 执行目标方法
   1. 代理对象执行目标方法
   2. CglibAopProxy.intercept()
      1. 得到目标方法的拦截器链（增强器包装成拦截器 MethodInterceptor ）
      2. 利用拦截器的链式机制，依次进入每一个拦截器进行执行；
      3. 效果（Spring 5.0）
         正常执行：前置通知→目标方法→返回通知→后置通知
         异常执行：前置通知→目标方法→异常通知→后置通知
```
![](src/docs/spring/AOP执行链执行流程.jpg)

---
#### Spring 监听器原理
![](src/docs/spring/Spring监听器原理.jpg)

---
MVC 整体架构
---

---
#### MVC启动原理
![](src/docs/mvc/MVC启动原理.jpg)

---
#### MVC启动过程
![](src/docs/mvc/MVC启动过程.jpg)

---
#### MVC请求处理流程
![](src/docs/mvc/MVC请求处理流程.jpg)

---
#### HandlerMapping与HandlerAdapter的交互
![](src/docs/mvc/HandlerMapping与HandlerAdapter的交互.jpg)

---
#### 方法执行流程
![](src/docs/mvc/方法执行流程.jpg)

---
#### @EnableWebMvc注解原理
![](src/docs/mvc/@EnableWebMvc注解原理.jpg)
