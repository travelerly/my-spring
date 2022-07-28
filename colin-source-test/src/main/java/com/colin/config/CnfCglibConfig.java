package com.colin.config;

import com.colin.bean.Car;
import com.colin.bean.Tank;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author colin
 * @create 2022-07-28 19:09
 */
//@Configuration
//@Component
public class CnfCglibConfig {

    /**
     * @Configuration、@Component、@ComponentScan、@Import、@ImportResource 等注解都可以注册配置类
     * 但只有标注了 @Configuration 注解的配置类称之为"full"配置类，标注了其它注解的配置类称为"lite"配置类
     *
     * 对于"full"配置类，其会被 Cglib 所代理，获取这个配置类对象，实际上获取的是代理对象，
     * 而"lite"配置类则为普通的配置类对象，获取这个配置类对象，即为原始配置类对象
     *
     * 如果在配置类中定义两个 @Bean 方法，在其中一个 @Bean 方法中调用另一个 @Bean 方法
     * 例如：car() 中调用 tank() 方法，当从容器中连续获取两次 Car 对象时，会因配置类的不同，而产生不同的结果：
     * 如果是"lite"配置类，Tank 会被创建两次
     * 如果是"full"配置类，Tank 只会被创建一次，因为配置类被 Cglib 代理了，方法被改写了
     */

    @Bean
    public Car car(){
        Car car = new Car();
        car.setName("colin");
        car.setTank(tank());
        return car;
    }

    @Bean
    public Tank tank(){
        return new Tank();
    }
}
