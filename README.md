# <img src="src/docs/spring-framework.png" width="80" height="80"> Spring Framework [![Build Status](https://ci.spring.io/api/v1/teams/spring-framework/pipelines/spring-framework-5.3.x/jobs/build/badge)](https://ci.spring.io/teams/spring-framework/pipelines/spring-framework-5.3.x?groups=Build") [![Revved up by Gradle Enterprise](https://img.shields.io/badge/Revved%20up%20by-Gradle%20Enterprise-06A0CE?logo=Gradle&labelColor=02303A)](https://ge.spring.io/scans?search.rootProjectNames=spring)

## Spring 整体架构

Spring 的底层所有资源（xml、注解、网络文件、磁盘文件等）都用 Resource 来表示，Spring 使用 ResourceLoader（资源加载器）加载这些资源，交给 BeanDefinitionReader 来读取，并存放到 Spring 工厂的 BeanDefinitionRegistry （Bean 定义信息注册中心）中，即 Spring 一启动，就将所有资源解析成 BeanDefinition 存入到 BeanDefinitionRegistry 中。（实际是保存在一个 map 中，BeanDefinitionMap），然后 Spring 将定义信息挨个创建成对象，并存入到 IOC 容器中，Spring 中使用各种池来存储对象，其中单例对象池用于保存所有的单例对象，在使用对象时，就去单例池中获取对象。

---
#### Spring架构原理图
![](src/docs/Spring架构原理图.jpg)

---
#### 容器刷新完整流程
![](src/docs/容器刷新完整流程.jpg)

---
#### Bean的初始化流程
![](src/docs/Bean的初始化流程.jpg)

---
#### Bean生命周期
![](src/docs/Bean生命周期.jpg)

---
#### 循环引用
![](src/docs/循环引用.jpg)

---
#### AOP定义阶段
![](src/docs/AOP定义阶段.jpg)

---
#### AOP增强流程
![](src/docs/AOP增强流程.jpg)

---
#### AOP介入流程
![](src/docs/AOP介入流程.jpg)

---
#### AOP执行链执行流程
![](src/docs/AOP执行链执行流程.jpg)



---