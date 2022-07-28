package com.colin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

/**
 * @author colin
 * @create 2022-07-28 15:19
 */
// 开启基于注解的 aop 功能
@EnableAspectJAutoProxy(exposeProxy = true)
@Component
public class AopConfig {
}
