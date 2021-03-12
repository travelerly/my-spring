package com.colin.bean;

import lombok.Data;
import lombok.ToString;

/**
 * @author colin
 * @create 2021-03-12 14:03
 */
@Data
@ToString
public class Person {

	public String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
