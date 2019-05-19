package ru.askhad.apishev.chartapp.model;

public class Point {
    private float x;
    private int y;
    private String name;


    public Point (String name) {
        this.name = name;
    }

    public Point (float x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
