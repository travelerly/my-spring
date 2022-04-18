package com.colin.aop.cycle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author colin
 * @create 2022-03-18 12:09
 */
//@Component
public class AService {

    @Autowired
    private BService bService;

    public AService() {
        System.out.println("=== AService 创建了 ===");
    }

    // 目标方法
    public String helloCycleAspect(String name){

        String result = "目标方法执行：你好，"+name;
        System.out.println(result);

		/*// 模拟异常
		Object o1 = new ArrayList<>(10).get(11);*/

        return "你好，返回通知";
    }
}
