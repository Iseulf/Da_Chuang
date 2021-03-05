package com.example.da_chuang.game.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Seulf
 * @version 1.0 2021/3/3
 */
public class SnakeBean {
    private List<PointBean> snake = new ArrayList<>();

    public List<PointBean> getSnake() {
        return snake;
    }

    public void setSnake(List<PointBean> snake) {
        this.snake = snake;
    }
}

