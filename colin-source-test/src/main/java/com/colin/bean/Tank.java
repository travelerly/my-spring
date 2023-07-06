package com.colin.bean;

/**
 * @author colin
 * @create 2022-07-28 19:00
 */
public class Tank {

    private String name;
    private Integer index;

    public Tank() {
        System.out.println("※※※※※ Tank 创建了 ※※※※※");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
}
