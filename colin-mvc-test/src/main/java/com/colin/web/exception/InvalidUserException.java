package com.colin.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author colin
 * @create 2021-05-04 22:09
 * 自定义异常
 */
@ResponseStatus(value = HttpStatus.CONFLICT,reason = "非法用户")
public class InvalidUserException extends RuntimeException{
	private static final long serialVersionUID = 4436401836539764556L;
}
