package com.colin.transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

/**
 * @author colin
 * @create 2022-07-25 19:04
 */
@Component
public class StudentServiceImpl implements StudentService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void findStudent(int studentId) {
        // 查询
        String sql = "select name from student where id="+studentId;
        String name = jdbcTemplate.queryForObject(sql, String.class);
        System.out.println("学生的名字是：" + name);
    }

    @Transactional
    @Override
    public void saveStudent(int age,String name,int exCode){
        String sql = "insert into student(age,name) values(?,?)";
        jdbcTemplate.update(sql, age, name);
        System.out.println("=====================================");
        // 模拟异常
        int x = 1 / exCode;
    }
}
