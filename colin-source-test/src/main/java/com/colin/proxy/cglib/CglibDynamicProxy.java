package com.colin.proxy.cglib;


import org.aopalliance.intercept.MethodInvocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.cglib.core.DebuggingClassWriter;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * cglib 动态代理
 * @author colin
 * @create 2022-03-31 09:21
 */
public class CglibDynamicProxy implements MethodInterceptor {

    /**
     * 目标对象
     */
    private Object target;

    public CglibDynamicProxy(Object target) {
        this.target = target;
    }

    /**
     * 获取目标对象的代理类
     * @return
     */
    public Object getProxy(){

        // 将生成的代理文件存储到项目根目录
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY,"./colin-source-test/src/main/java/com/colin/proxy/cglib/file");

        // 创建字节码增强器，可以为没有实现接口的类创建代理对象
        Enhancer enhancer = new Enhancer();
        // 因为 cglib 的原理是动态生成要代理类的子类，然后子类重写父类方法，所以这里要设置生成代理类的父类类型
        enhancer.setSuperclass(target.getClass());
        // 设置回调方法
        enhancer.setCallback(this);
        // 创建代理对象
        return enhancer.create();
    }

    /**
     * 对目标对象的方法进行增强
     * @param o
     * @param method
     * @param args
     * @param methodProxy
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        // 1.增强逻辑，执行方法前记录入参信息
        System.out.println("[记录日志] 准备执行方法" + method.getName() + "，参数列表：" + Arrays.toString(args));

        // 2.调用目标对象方法
        Object result = methodProxy.invoke(target, args);

        // 3.增强逻辑，执行方法后记录出参信息
        System.out.println("[记录日志] 方法执行结束" + method.getName() + "，返回值为：" + result);

        return result;
    }
}
