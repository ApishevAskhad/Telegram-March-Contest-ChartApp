package ru.askhad.apishev.chartapp.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Info {
    private String timeStamp;
    private Point[] points;

    public void setTimeStamp(Long time) {
        Date date = new Date(time);
        SimpleDateFormat fmt = new SimpleDateFormat("E, MMM d", new Locale("en"));
        this.timeStamp = fmt.format(date);
    }

    public void setPoints(Point[] points) {
        this.points = points;
    }

    public Point[] getPoints() {
        return points;
    }

    public String getTimeStamp() {
        return timeStamp;
    }
}
