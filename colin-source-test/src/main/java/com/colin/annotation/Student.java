package com.colin.annotation;

/**
 * @author colin
 * @create 2022-03-29 09:27
 */
@MyComponent(value = "test-value")
public class Student {
    private String name;
    private int age;

    public void study(){
        System.out.println("这是学生的学习方法");
    }
}
