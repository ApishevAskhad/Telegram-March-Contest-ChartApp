package ru.askhad.apishev.chartapp;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.askhad.apishev.chartapp.model.Chart;
import ru.askhad.apishev.chartapp.model.Graph;
import ru.askhad.apishev.chartapp.utils.ParseJSON;
import ru.askhad.apishev.chartapp.view.GraphView;
import ru.askhad.apishev.chartapp.view.SliderBackgroundGraphView;
import ru.askhad.apishev.chartapp.view.SliderView;

public class MainActivity extends AppCompatActivity {
    private SliderView sliderView;
    private LinearLayout layout;
    private LinearLayout rootLayout;
    private Chart[] charts;
    private Chart currentChart;
    private Spinner chartSpinner;
    private GraphView graphView;
    private SliderBackgroundGraphView sliderBackgroundGraphView;
    private MenuItem nightModeItem;
    private MenuItem dayModeItem;
    private Map<String, Boolean> checkBoxState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layout = findViewById(R.id.linear_lay);
        rootLayout = findViewById(R.id.root_layout);
        sliderView = findViewById(R.id.slider);
        graphView = findViewById(R.id.graph_view);
        chartSpinner = findViewById(R.id.chart_spinner);
        sliderBackgroundGraphView = findViewById(R.id.slider_background_graph);

        charts = new ParseJSON(this, readFile()).getCharts();
        setCurrentChart(0);
        checkBoxState = new HashMap<>();

        setChartSpinner();
        setCheckBoxes();

        graphView.setChart(currentChart);

        sliderView.addObserver(graphView);
        if (savedInstanceState != null) {
            float left = savedInstanceState.getFloat("left");
            int right = savedInstanceState.getInt("right");
            sliderView.setLeftBorder(left, right);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        nightModeItem = menu.findItem(R.id.night_mode_item);
        dayModeItem = menu.findItem(R.id.day_mode_item);
        dayModeItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.night_mode_item) {
            rootLayout.setBackgroundColor(getResources().getColor(R.color.nightMode));
            item.setVisible(false);
            graphView.setNightMode(true);
            dayModeItem.setVisible(true);
        }
        if (item.getItemId() == R.id.day_mode_item) {
            rootLayout.setBackgroundColor(getResources().getColor(R.color.dayMode));
            item.setVisible(false);
            graphView.setNightMode(false);
            nightModeItem.setVisible(true);
        }
        return true;
    }

    private void setCurrentChart(int index) {
        this.currentChart = charts[index];
    }

    private void setChartSpinner() {
        List<String> spinnerData = new ArrayList<>();
        for (int i = 0; i < charts.length; i++)
            spinnerData.add("Chart " + i);
        ArrayAdapter<String> chartArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerData);
        chartArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chartSpinner.setAdapter(chartArrayAdapter);
        chartSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setCurrentChart(position);
                currentChart.setAllGraphsVisible();
                graphView.setChart(currentChart);
                sliderBackgroundGraphView.setChart(currentChart);
                setCheckBoxes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void setCheckBoxes() {
        layout.removeAllViewsInLayout();
        for (final Graph g : currentChart.getAllGraphs()) {
            checkBoxState.put(g.getName(), g.getVisible());
            AppCompatCheckBox checkBox = new AppCompatCheckBox(this);
            checkBox.setText(g.getName());
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_checked},
                            new int[]{android.R.attr.state_checked}
                    },
                    new int[]{
                            Color.WHITE,
                            g.getColor()
                    }
            );
            checkBox.setSupportButtonTintList(colorStateList);
            checkBox.setChecked(checkBoxState.get(g.getName()));
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    g.setVisible(isChecked);
                    checkBoxState.put(g.getName(), isChecked);
                    graphView.update();
                    sliderBackgroundGraphView.update();
                }
            });
            layout.addView(checkBox);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /*for (String name : checkBoxState.keySet()) {
            outState.putBoolean(name, checkBoxState.get(name));
        }*/
        outState.putFloat("left", sliderView.getSliderX());
        outState.putInt("right", sliderView.getSliderWidth());

    }

    private String readFile() {
        String s = "";
        try {
            InputStream input = getResources().getAssets().open("chart_data.json");
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();
            s = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }
}
