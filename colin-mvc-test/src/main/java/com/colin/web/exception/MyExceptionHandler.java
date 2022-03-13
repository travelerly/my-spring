package com.colin.web.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author colin
 * @create 2021-05-05 15:04
 * 全局异常处理（Controller 的拦截器）
 *
 * @ControllerAdvice 专门处理异常，默认加载容器中
 */
@ControllerAdvice
public class MyExceptionHandler {

	// 方法参数详细参考：https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-ann-exceptionhandler-args
	// 方法返回值详细参考：https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-ann-exceptionhandler-return-values
	@ResponseBody
	@ExceptionHandler(value = {ArithmeticException.class})
	public String handleZeroException(Exception exception){
		return "算数异常";
	}
}
