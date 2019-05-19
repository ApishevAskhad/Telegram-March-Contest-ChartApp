package ru.askhad.apishev.chartapp.model;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class Chart {
    private Graph[] graphs; //графики чарта
    private int pointsCount; //кол-во точек на чарте
    private String[] formatedTimes; //временной отрезок
    private Long[] times;
    private Context context;

    public Chart(Context context, JSONObject chartJSON) {
        this.context = context;
        parseJSON(chartJSON);
        setPointsCount();
    }

    private void setPointsCount() {
        //todo check for all graph.length() is identical; if not - return set 0
        this.pointsCount = graphs[0].getPointsCount();
    }

    public int getPointsCount() {
        return pointsCount;
    }

    private void parseJSON(JSONObject chartJSON) {
        try {
            JSONArray columns = chartJSON.getJSONArray("columns");
            for (int i = 0; i < columns.length(); i++) {
                JSONArray keys = columns.getJSONArray(i);
                if ("x".equals(keys.get(0))) {
                    initTimes(keys.length() - 1);
                    setTimes(keys);
                    setFormatedTimes();
                    break;
                }
            }
            //length - количество графов в структуре
            int length = chartJSON.getJSONObject("names").length();
            initGraph(length);
            Iterator<String> iter = chartJSON.getJSONObject("names").keys();
            int i = 0;
            while (iter.hasNext()) {
                Graph graph = new Graph();
                String keyName = iter.next();
                String name = chartJSON.getJSONObject("names").getString(keyName);
                graph.setName(name);
                String color = chartJSON.getJSONObject("colors").getString(keyName);
                graph.setColor(color);
                for (int j = 0; j < columns.length(); j++) {
                    JSONArray key = columns.getJSONArray(j);
                    if (keyName.equals(key.get(0))) {
                        graph.initPoints(key.length() - 1);
                        for (int k = 1; k < key.length(); k++)
                            graph.setPoint(k - 1, key.getInt(k));
                        break;
                    }
                }
                setGraph(i, graph);
                i++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setTimes(JSONArray timesJSON) {
        for (int i = 1; i < timesJSON.length(); i++) {
            try {
                times[i - 1] = Long.parseLong(timesJSON.getString(i));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Long getTimeByIndex(int index) {
        index = (index < 0) ? 0 : index;
        index = (index > times.length) ? times.length - 1 : index;
        return times[index];
    }

    private void initTimes(int length) {
        this.formatedTimes = new String[length];
        this.times = new Long[length];
    }

    private void setTime(int index, String time) {
        this.formatedTimes[index] = time;
    }

    private void setFormatedTimes() {
        for (int i = 0; i < times.length; i++) {
            Date date = new Date(times[i]);
            SimpleDateFormat fmt = new SimpleDateFormat("MMM d", new Locale("en"));
            setTime(i, fmt.format(date));
        }
    }

    public String[] getFormatedTimes() {
        return this.formatedTimes;
    }

    private void initGraph(int length) {
        this.graphs = new Graph[length];
    }

    private void setGraph(int index, Graph graph) {
        this.graphs[index] = graph;
    }

    public Graph[] getVisibleGraphs() {
        int length = 0;
        for (Graph g : graphs) {
            if (g.getVisible())
                length++;
        }
        Graph[] visibleGraphs = new Graph[length];
        int i = 0;
        for (Graph g : graphs) {
            if (g.getVisible())
                visibleGraphs[i++] = g;
        }
        return visibleGraphs;
    }

    public Graph[] getAllGraphs() {
        return this.graphs;
    }

    public void setAllGraphsVisible() {
        for (Graph g : graphs)
            g.setVisible(true);
    }
}
