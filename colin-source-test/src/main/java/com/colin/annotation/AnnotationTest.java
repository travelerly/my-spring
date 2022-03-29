package com.colin.annotation;

/**
 * @author colin
 * @create 2022-03-29 09:29
 */
public class AnnotationTest {

    public static void main(String[] args) {
        // 获取学生类的 Class 对象
        Class<Student> studentClass = Student.class;
        // 判断学生了是否标注了 @MyComponent 注解
        if (studentClass.isAnnotationPresent(MyComponent.class)){
            // 获取学生类上标注的 @MyComponent 注解
            MyComponent annotation = studentClass.getAnnotation(MyComponent.class);
            System.out.println("Student 类上标注 @MyComponent 注解，value = " + annotation.value() +
                    "，这里可以做一些特殊处理，即给注解添加功能");
        } else {
            System.out.println("Student 类上没有标注 @MyComponent 注解");
        }
    }
}
