package com.colin.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * @author colin
 * @create 2022-07-28 15:20
 */
// 开启基于注解的事务功能
//@EnableTransactionManagement
//@Component
public class TxConfig {
    @Bean
	@Lookup
    public DataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUsername("root");
        dataSource.setPassword("root1024");
        dataSource.setUrl("jdbc:mysql://localhost:3306/spring_jdbc");
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        return dataSource;
    }

    /**
     *  配置JdbcTemplate Bean组件
     *     <bean class="org.springframework.jdbc.core.JdbcTemplate" id="jdbcTemplate">
     *         <property name="dataSource" ref="dataSource" ></property>
     *     </bean>
     * @param dataSource
     * @return
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     *  配置事务管理器
     *     <bean class="org.springframework.jdbc.datasource.DataSourceTransactionManager" id="transactionManager">
     *         <property name="dataSource" ref="dataSource"></property>
     *     </bean>
     * @param dataSource
     * @return
     */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
