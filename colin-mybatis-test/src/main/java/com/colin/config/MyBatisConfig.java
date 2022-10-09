package com.colin.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.colin.bean.User;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * @author colin
 * @create 2022-09-09 10:51
 */
@EnableTransactionManagement
@Configuration
@MapperScan(basePackages = {"com.colin.mapper"})
@ComponentScan(basePackages = {"com.colin"})
@Repository
public class MyBatisConfig {

    /**
     * <bean class="com.alibaba.druid.pool.DruidDataSource" id="dataSource">  </bean>
     *
     * <bean class="org.mybatis.spring.SqlSessionFactoryBean" id="sqlSessionFactory">
     *     datasource
     *     mapper文件的路径
     *     别名
     * </bean>
     *
     * <mapper-scan basePackage=""/>
     * @return
     * @throws IOException
     */
    @Bean    // =====  >    <bean class="org.mybatis.spring.SqlSessionFactoryBean">
    public SqlSessionFactoryBean sqlSessionFactory( ) throws IOException {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource());
        // 设置 MyBatis 配置文件路径
        factoryBean.setConfigLocation(new ClassPathResource("mybatis/mybatis-config.xml"));
        // 设置 SQL 映射文件路径
        factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mybatis/mapper/*.xml"));
        factoryBean.setTypeAliases(User.class);
        return factoryBean;
    }

    public DataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUsername("root");
        dataSource.setPassword("root1024");
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/mybatis_example");
        return dataSource;
    }
}
