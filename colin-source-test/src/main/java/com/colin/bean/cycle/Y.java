package com.colin.bean.cycle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author colin
 * @create 2021-11-10 16:17
 */
//@Component
public class Y {

	public Y() {
		System.out.println("Y 创建了");
	}

	@Autowired
	private X x;
}
