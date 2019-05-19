package ru.askhad.apishev.chartapp.model;

import android.graphics.Color;

public class Graph {
    private String name; //имя графа json -> name
    private int color; //цвект графа json -> colors
    private int[] points; //точки графа по оси У
    private boolean visible; //флаг: отображать ли граф в чарте?

    public Graph() {
        setVisible(true);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setColor(String hexColor) {
        this.color = Color.parseColor(hexColor);
    }

    public int getColor() {
        return this.color;
    }

    public void initPoints(int size) {
        this.points = new int[size];
    }

    public int getPointsCount() {
        return this.points.length;
    }

    public void setPoint(int index, int value) {
        this.points[index] = value;
    }

    public int[] getPoints() {
        return this.points;
    }

    public void setVisible(boolean visibility) {
        this.visible = visibility;
    }

    public boolean getVisible() {
        return this.visible;
    }
}

