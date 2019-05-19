package ru.askhad.apishev.chartapp.utils;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.askhad.apishev.chartapp.model.Chart;

public class ParseJSON {
    private String json;
    private Chart[] charts;
    private Context context;

    public ParseJSON(Context context, String json) {
        this.context = context;
        this.json = json;
        parse();
    }

    private void parse() {
        if (json.charAt(0) == '[') {
            try {
                JSONArray arrayJSON = new JSONArray(json);
                initChart(arrayJSON.length());
                for (int i = 0; i < arrayJSON.length(); i++) {
                    JSONObject chartJSON = arrayJSON.getJSONObject(i);
                    setChart(i, new Chart(context, chartJSON));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void initChart(int length) {
        this.charts = new Chart[length];
    }

    private void setChart(int index, Chart chart) {
        this.charts[index] = chart;
    }

    public Chart[] getCharts() {
        return this.charts;
    }
}
