package com.colin.bean.circle;

import org.springframework.stereotype.Component;

/**
 * @author colin
 * @create 2021-03-20 15:50
 */
@Component
public class B {

	private A a;

	public B() {
		System.out.println("===B...构造...===");
	}

	public void setA(A a) {
		this.a = a;
	}
}
