package com.colin.aware;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.stereotype.Component;

/**
 * Aware 接口也称为 Spring 的感知接口，当 bean 实现了 aware 接口，
 * Spring 在实例化这些 bean 的时候，就会通知感知接口中的方法注入相应的数据
 *
 * @author colin
 * @create Beanna-28 11:20
 */
//@Component
public class Student implements BeanNameAware {

    private String name = "colin";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setBeanName(String name) {
        System.out.println("student beanName：" + name);
    }
}
