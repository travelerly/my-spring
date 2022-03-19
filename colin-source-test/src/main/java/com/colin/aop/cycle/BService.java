package com.colin.aop.cycle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author colin
 * @create 2022-03-18 12:10
 */
@Component
public class BService {

    @Autowired
    private AService aService;

    public BService() {
        System.out.println("=== BService 创建了 ===");
    }
}
