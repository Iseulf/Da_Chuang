package com.example.da_chuang.game.bean;

/**
 * @author Seulf
 * @version 1.0 2021/3/4
 */
public class FoodBean {
    private int x;
    private int y;

    public FoodBean() {
        this.x = (int) (Math.random() * 30);
        this.y = (int) (Math.random() * 30);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void change() {
        this.x = (int) (Math.random() * 30);
        this.y = (int) (Math.random() * 30);
    }
}
