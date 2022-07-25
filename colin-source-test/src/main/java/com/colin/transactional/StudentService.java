package com.colin.transactional;

/**
 * @author colin
 * @create 2022-07-25 19:03
 */
public interface StudentService {

    void findStudent(int studentId);

    void saveStudent(int age,String name,int exCode);
}
