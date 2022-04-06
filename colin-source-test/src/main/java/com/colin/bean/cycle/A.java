package com.colin.bean.cycle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * @author colin
 * @create 2021-03-20 15:50
 */
@Component
public class A {

	@Autowired
	private B b;

	public A() {
		System.out.println("===A...构造...===");
	}

}
