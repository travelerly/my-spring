package com.colin.transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author colin
 * @create 2022-07-25 20:33
 */
//@Component
public class CglibStudentService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void findStudent(int studentId) {
        // 查询
        String sql = "select name from student where id="+studentId;
        String name = jdbcTemplate.queryForObject(sql, String.class);
        System.out.println("学生的名字是：" + name);
    }

    @Transactional
    public void saveStudent(int age,String name,int exCode){
        String sql = "insert into student(age,name) values(?,?)";
        jdbcTemplate.update(sql, age, name);
        System.out.println("=====================================");
        // 模拟异常
        int x = 1 / exCode;
    }
}
