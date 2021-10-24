package com.colin.bean.cycle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author colin
 * @create 2021-03-20 15:50
 */
//@Component
public class A {

	private B b;

	public A() {
		System.out.println("===A...构造...===");
	}

	@Autowired
	public void setB(B b) {
		this.b = b;
	}
}
