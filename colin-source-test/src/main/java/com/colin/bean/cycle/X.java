package com.colin.bean.cycle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author colin
 * @create 2021-11-10 16:16
 */
//@Component
public class X {

	public X() {
		System.out.println("X 创建了");
	}

	@Autowired
	private Y y;
}
