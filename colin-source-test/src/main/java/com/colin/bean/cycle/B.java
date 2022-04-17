package com.colin.bean.cycle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author colin
 * @create 2021-03-20 15:50
 */
@Component
public class B {

	/*@Autowired
	private A a;*/

	public B() {
		System.out.println("===B...构造...===");
	}

}
