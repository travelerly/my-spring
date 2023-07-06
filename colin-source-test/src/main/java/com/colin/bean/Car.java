package com.colin.bean;

/**
 * @author colin
 * @create 2022-07-28 19:00
 */
public class Car {

    private String name;
    private Tank tank;

    public Car() {
        System.out.println("※※※※※ Car  创建了 ※※※※※");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Tank getTank() {
        return tank;
    }

    public void setTank(Tank tank) {
        this.tank = tank;
    }
}
