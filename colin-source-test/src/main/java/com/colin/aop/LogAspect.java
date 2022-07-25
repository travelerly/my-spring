package com.colin.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author colin
 * @create 2021-03-23 16:48
 *
 * spring 5.0
 * 正常执行顺序：前置通知-->目标方法-->返回通知-->后置通知
 * 异常执行顺序：前置通知-->目标方法-->异常通知-->后置通知
 *
 * try{
 *     前置通知
 *     目标方法
 *     返回通知
 * }catch(){
 *     异常通知
 * }finally{
 *     后置通知
 * }
 *
 * 「@Aspect」：表明为切面类
 * 切面类也要注册进容器中「@Component」
 */
//@Component
//@Aspect
public class LogAspect {

	public LogAspect() {
		System.out.println("...LogAspect创建了...");
	}

	/**
	 * 前置通知，增强方法/增强器
	 * @param joinPoint 封装了 AOP 中切面方法的信息
	 */
	@Before("execution(* com.colin.aop.HelloService.sayHello(..))")
	public void logStart(JoinPoint joinPoint){
		String name = joinPoint.getSignature().getName();
		System.out.println("前置通知logStart==>"+name+"===【args:"+ Arrays.asList(joinPoint.getArgs())+"】");
	}

	/**
	 * 返回通知
	 * @param joinPoint 封装了 AOP 中切面方法的信息
	 * @param result 目标方法的返回值
	 */
	@AfterReturning(value = "execution(* com.colin.aop.HelloService.sayHello(..))",returning = "result")
	public void logReturn(JoinPoint joinPoint,Object result){
		String name = joinPoint.getSignature().getName();
		System.out.println("返回通知logReturn==>"+name+"===【args:"+ Arrays.asList(joinPoint.getArgs())+"】【result："+result);
	}

	/**
	 * 后置通知
	 * @param joinPoint 封装了 AOP 中切面方法的信息
	 */
	@After("execution(* com.colin.aop.HelloService.sayHello(..))")
	public void logEnd(JoinPoint joinPoint){
		String name = joinPoint.getSignature().getName();
		System.out.println("后置通知logEnd==>"+name+"===【args:"+ Arrays.asList(joinPoint.getArgs())+"】");
	}

	/**
	 * 异常通知
	 * @param joinPoint 封装了 AOP 中切面方法的信息
	 * @param e 异常
	 */
	@AfterThrowing(value = "execution(* com.colin.aop.HelloService.sayHello(..))",throwing = "e")
	public void logError(JoinPoint joinPoint,Exception e){
		String name = joinPoint.getSignature().getName();
		System.out.println("异常通知logError==>"+name+"===【args:"+ Arrays.asList(joinPoint.getArgs())+"】【exception: \"+e+\"】\"");
	}


}
