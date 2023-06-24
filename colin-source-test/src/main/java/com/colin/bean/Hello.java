package com.colin.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author colin
 * @create 2021-03-19 09:03
 */
@Component
public class Hello {

    public Hello() {
        System.out.println("※※※※※※※※※※※※hello 正在创建※※※※※※※※※※※※");
    }
}
