package com.colin.bean.circle;

import org.springframework.stereotype.Component;

/**
 * @author colin
 * @create 2021-03-20 15:50
 */
@Component
public class A {

	private B b;

	public A() {
		System.out.println("===A...构造...===");
	}

	public void setB(B b) {
		this.b = b;
	}
}
