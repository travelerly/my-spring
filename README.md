# <img src="src/docs/spring-framework.png" width="80" height="80"> Spring Framework 

## Spring 整体架构

#### Spring 如何工作
Spring 暴露给程序员的使用方式是，要么写一个 xml 文件、要么使用注解、要么利用磁盘文件、网络文件等，把需要的功能定义出来，这个信息最终会生成一个组件或者功能配置清单，Spring 会去读取并解析这些功能清单，这些信息就会决定 Spring 框架中的各种行为。

#### Spring 整体架构流程
在 Spring 的底层把所有的资源（xml、注解、网络文件、磁盘文件等）都用 Resource 来表示，Spring 使用 ResourceLoader（资源加载器）加载这些资源，交给 BeanDefinitionReader 来读取和解析，并存放到 Spring 工厂的 BeanDefinitionRegistry （Bean  定义信息注册中心）中，即 Spring 一启动，就将所有资源解析成 BeanDefinition 存入到 BeanDefinitionRegistry 中。（实际是保存在一个 map 中，BeanDefinitionMap），然后 Spring 将这些 bean 的定义信息挨个创建成对象，并存入到 IOC 容器中，Spring 中使用各种池来存储对象，其中单例对象池用于保存所有的单例对象，在使用对象时，就去单例池中获取对象。

---
#### Spring架构原理图
![](src/docs/spring/Spring架构原理图.jpg)

---
#### 容器刷新完整流程
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
#### AOP定义阶段
![](src/docs/spring/AOP定义阶段.jpg)

---
#### AOP增强流程
![](src/docs/spring/AOP增强流程.jpg)

---
#### AOP介入流程
![](src/docs/spring/AOP介入流程.jpg)

---
#### AOP执行链执行流程
![](src/docs/spring/AOP执行链执行流程.jpg)



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
