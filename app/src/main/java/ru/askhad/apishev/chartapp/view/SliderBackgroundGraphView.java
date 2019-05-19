package ru.askhad.apishev.chartapp.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.Arrays;

import ru.askhad.apishev.chartapp.model.Chart;
import ru.askhad.apishev.chartapp.model.Graph;

public class SliderBackgroundGraphView extends View implements Update {
    private int screenWidth;
    private int screenHeight;
    private Chart chart;
    private Graph[] graphs;
    private float[] xCoordinates;
    private int max;
    private Paint paint;
    private Path path;

    public SliderBackgroundGraphView(Context context) {
        super(context);
        init();
    }

    public SliderBackgroundGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SliderBackgroundGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        path = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        screenWidth = getMeasuredWidth() + getPaddingLeft() + getPaddingRight();
        screenHeight = getMeasuredHeight() + getPaddingBottom() + getPaddingTop();
        setMeasuredDimension(screenWidth, screenHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (chart != null)
            drawSliderBackgroundGraph(canvas);
    }

    @Override
    public void update() {
        this.graphs = chart.getVisibleGraphs();
        setMax();
        invalidate();
    }

    @Override
    public void update(int left, int right) {

    }

    public void setChart(Chart chart) {
        this.chart = chart;
        this.graphs = chart.getAllGraphs();
        setxCoordinates();
        setMax();
        invalidate();
    }

    private void setxCoordinates() {
        if (chart != null) {
            int length = chart.getPointsCount();
            xCoordinates = new float[length];
            for (int i = 0; i < length - 1; i++) {
                xCoordinates[i] = i * (screenWidth * 1f / (length - 1));
            }
            xCoordinates[length - 1] = screenWidth * 1f;
        }
    }

    private void setMax() {
        if (graphs.length > 0) {
            int length = graphs.length;
            int[] maxValues = new int[length];
            int tmp = 0;
            for (int i = 0; i < length; i++) {
                for (int y : graphs[i].getPoints()) {
                    if (y > tmp)
                        tmp = y;
                }
                maxValues[i] = tmp;
            }
            Arrays.sort(maxValues);
            this.max = maxValues[length - 1];
        }
    }

    private int getMax() {
        return this.max;
    }

    private void drawSliderBackgroundGraph(Canvas canvas) {
        for (Graph g : graphs) {
            path.reset();
            paint.reset();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1f);
            paint.setColor(g.getColor());
            double scale = screenHeight * 1.0 / getMax();
            int[] points = g.getPoints();
            path.moveTo(xCoordinates[0], screenHeight - (int) (points[0] * scale));
            for (int i = 1; i < xCoordinates.length; i++)
                path.lineTo(xCoordinates[i], screenHeight - (int) (points[i] * scale));
            canvas.drawPath(path, paint);
        }
    }
}
