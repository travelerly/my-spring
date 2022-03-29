package com.colin.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author colin
 * @create 2022-03-29 09:47
 *
 * 注解的本质就是一个接口，是一个继承了 Annotation 接口的接口，可以通过反编译注解的 class 文件来进行验证
 *
 * 自定义注解时，必须搭配元注解一起使用
 * 元注解 @Retention：表示所定义的注解什么时候有效，
 * RetentionPolicy.RUNTIME：代码运行时有效
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MyComponent {
    String value() default "";
}
