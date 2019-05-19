package ru.askhad.apishev.chartapp.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.askhad.apishev.chartapp.R;
import ru.askhad.apishev.chartapp.model.Chart;
import ru.askhad.apishev.chartapp.model.Graph;
import ru.askhad.apishev.chartapp.model.Info;
import ru.askhad.apishev.chartapp.model.Point;

public class GraphView extends View implements Update {
    private int screenWidth;
    private int screenHeight;
    private int left;
    private int right;
    private Chart chart;
    private Graph[] graphs;
    private float textSize;
    private int offset;
    private int max;
    private Paint paint;
    private Path path;
    private float[] xCoordinatesLocal;
    private float[] xCoordinatesAbsolute;
    private List<List<Integer>> positions;
    private Info info;
    private boolean showInfo;
    private boolean showData;
    private boolean nightMode;


    public GraphView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public GraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.GraphView, defStyleAttr, 0);
        try {
            textSize = array.getDimension(R.styleable.GraphView_textSize, 0);
        } finally {
            array.recycle();
        }
        offset = 30;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        path = new Path();
        positions = new ArrayList<>();
        info = new Info();
    }

    @Override
    public void update() {
        this.graphs = chart.getVisibleGraphs();
        this.showData = (graphs.length > 0);
        setPositions();
        invalidate();
    }

    @Override
    public void update(int left, int right) {
        this.left = (left < 0) ? 0 : left;
        this.right = (right > screenWidth) ? screenWidth : right;
        update();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        screenWidth = getMeasuredWidth() + getPaddingRight() + getPaddingLeft();
        screenHeight = getMeasuredHeight() + getPaddingBottom() + getPaddingTop();
        setMeasuredDimension(screenWidth, screenHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (chart != null) {
            if (showData) {
                drawGraph(canvas);
                drawTimeLine(canvas);
                drawScale(canvas);
                drawLines(canvas);
                if (showInfo) {
                    drawInfoLine(canvas);
                    drawInfoCircles(canvas, 7, 4);
                    drawInfoTable(canvas, 5, 10, 15);
                }
            } else {
                drawDataMessage(canvas);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                showInfo = true;
                setInfoIndex(e.getX());
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                showInfo = false;
                break;
        }
        return true;
    }

    public void setNightMode(boolean value) {
        this.nightMode = value;
        invalidate();
    }

    //todo interface Informational
    @SuppressLint("Assert")
    private void setInfoIndex(float x) {
        Point[] infoPoints = new Point[graphs.length];
        for (int i = 0; i < infoPoints.length; i++)
            infoPoints[i] = new Point(graphs[i].getName());
        assert (positions.size() == infoPoints.length);
        for (int j = 0; j < positions.size(); j++) {
            for (int i = 0; i < positions.get(j).size(); i++) {
                if (xCoordinatesLocal[i] >= (int) x) {
                    infoPoints[j].setX(xCoordinatesLocal[i]);
                    infoPoints[j].setY(positions.get(j).get(i));
                    break;
                }
            }
        }
        x = left + ((right - left) / (screenWidth * 1f / x));
        int i = 0;
        while (xCoordinatesAbsolute[i++] <= x) ;
        info.setTimeStamp(chart.getTimeByIndex(i));
        info.setPoints(infoPoints);
        invalidate();
    }

    public void setChart(Chart chart) {
        this.chart = chart;
        this.graphs = chart.getAllGraphs();
        this.showData = (chart.getVisibleGraphs().length > 0);
        setPositions();
        invalidate();
    }

    private void setxCoordinatesAbsolute() {
        int length = chart.getPointsCount();
        xCoordinatesAbsolute = new float[length];
        for (int i = 0; i < length - 1; i++)
            xCoordinatesAbsolute[i] = i * (screenWidth * 1f / (length - 1));
        xCoordinatesAbsolute[length - 1] = screenWidth;
    }

    public void setPositions() {
        positions.clear();
        setxCoordinatesAbsolute();
        if (graphs.length > 0) {
            int length = chart.getPointsCount();
            //todo check getPointsCount() must be more or equal than 2
            for (Graph g : graphs) {
                int[] points = g.getPoints();
                List<Integer> yValues = new ArrayList<>();
                for (int i = 0; i < length; i++) {
                    if (xCoordinatesAbsolute[i] >= left && xCoordinatesAbsolute[i] <= right)
                        yValues.add(points[i]);
                }
                positions.add(yValues);
            }
            int size = positions.get(0).size();
            xCoordinatesLocal = new float[size];
            for (int i = 0; i < size - 1; i++) {
                xCoordinatesLocal[i] = i * (screenWidth * 1f / (size - 1));
            }
            xCoordinatesLocal[size - 1] = screenWidth * 1f;
            setMax();
        }
    }

    private void setMax() {
        int length = graphs.length;
        int[] maxValues = new int[length];
        int tmp = 0;
        for (int i = 0; i < length; i++) {
            List<Integer> map = positions.get(i);
            for (Integer v : map) {
                if (v > tmp)
                    tmp = v;
            }
            maxValues[i] = tmp;
        }
        Arrays.sort(maxValues);
        this.max = maxValues[length - 1];
    }

    private int getMax() {
        return this.max;
    }

    private void drawDataMessage(Canvas canvas) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(Math.round(textSize * getResources().getDisplayMetrics().scaledDensity));
        paint.setColor(getResources().getColor(R.color.rulerTextColor));
        String message = getResources().getString(R.string.no_data_message);
        float messageWidth = paint.measureText(message);
        Rect textBounds = new Rect();
        paint.getTextBounds(message, 0, message.length(), textBounds);
        float messageHeight = textBounds.height();
        canvas.drawText(message, (screenWidth * 1f / 2) - (messageWidth / 2), (screenHeight * 1f / 2) - (messageHeight / 2), paint);
    }

    private void drawInfoCircles(Canvas canvas, int radiusOut, int radiusIn) {
        for (int i = 0; i < graphs.length; i++) {
            paint.reset();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(graphs[i].getColor());
            double scale = (screenHeight - offset) * 1.0 / getMax();
            canvas.drawCircle(info.getPoints()[i].getX(), screenHeight - offset - (int) (info.getPoints()[i].getY() * scale), radiusOut, paint);
            if (nightMode) {
                paint.setColor(getResources().getColor(R.color.nightMode));
            } else {
                paint.setColor(Color.WHITE);
            }
            canvas.drawCircle(info.getPoints()[i].getX(), screenHeight - offset - (int) (info.getPoints()[i].getY() * scale), radiusIn, paint);
        }
    }

    private void drawInfoLine(Canvas canvas) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(getResources().getColor(R.color.rulerTextColor));
        float x = info.getPoints()[0].getX();
        canvas.drawLine(x, 0, x, screenHeight - offset, paint);
    }

    private void drawInfoTable(Canvas canvas, int tablePading, int tableSpace, int tableOffset) {
        paint.reset();
        paint.setTextSize(Math.round(textSize * getResources().getDisplayMetrics().scaledDensity));
        int size = graphs.length + 1;
        float[] widths = new float[size];
        int[] heights = new int[size];
        String[] words = new String[size];
        words[0] = info.getTimeStamp();
        for (int i = 0; i < graphs.length; i++) {
            words[i + 1] = graphs[i].getName() +
                    ": " +
                    info.getPoints()[i].getY();
        }
        Rect rect = new Rect();
        paint.getTextBounds(words[0], 0, words[0].length(), rect);
        heights[0] = rect.height();
        widths[0] = paint.measureText(words[0]);
        for (int i = 0; i < graphs.length; i++) {
            widths[i + 1] = paint.measureText(words[i + 1]);
            paint.getTextBounds(words[i + 1], 0, words[i + 1].length(), rect);
            heights[i + 1] = rect.height();
        }

        float maxWidth = 0;
        for (float x : widths) {
            if (x > maxWidth)
                maxWidth = x;
        }

        int heightSum = 0;
        for (int x : heights) {
            heightSum += x;
        }

        int tableWidth = screenWidth - tableOffset - (int) maxWidth - (tablePading * 2);
        int tableHeight = (((size + 1) * tableSpace) + heightSum) + tableOffset;

        //table background
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        if (nightMode) {
            paint.setColor(getResources().getColor(R.color.nightMode));
        } else {
            paint.setColor(Color.WHITE);
        }
        paint.setAlpha(160);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(tableWidth, tableOffset, screenWidth - tableOffset, tableHeight, 5, 5, paint);
        } else {
            canvas.drawRect(tableWidth, tableOffset, screenWidth - tableOffset, tableHeight, paint);
        }

        //table border
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(getResources().getColor(R.color.rulerTextColor));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(tableWidth, tableOffset, screenWidth - tableOffset, tableHeight, 5, 5, paint);
        } else {
            canvas.drawRect(tableWidth, tableOffset, screenWidth - tableOffset, tableHeight, paint);
        }

        int[] heightPositions = new int[size];
        heightPositions[0] = tableSpace + heights[0];
        for (int i = 1; i < size; i++)
            heightPositions[i] = heightPositions[i - 1] + (tableSpace + heights[i]);

        //draw date
        paint.reset();
        paint.setAntiAlias(true);
        paint.setTextSize(Math.round(textSize * getResources().getDisplayMetrics().scaledDensity));
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getResources().getColor(R.color.rulerTextColor));
        canvas.drawText(words[0], tableWidth + tablePading, heightPositions[0] + tableOffset, paint);

        //draw points data
        paint.reset();
        paint.setAntiAlias(true);
        paint.setTextSize(Math.round(textSize * getResources().getDisplayMetrics().scaledDensity));
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        for (int i = 0; i < graphs.length; i++) {
            paint.setColor(graphs[i].getColor());
            canvas.drawText(words[i + 1], tableWidth + tablePading, heightPositions[i + 1] + tableOffset, paint);
        }

    }

    private void drawGraph(Canvas canvas) {
        for (int i = 0; i < graphs.length; i++) {
            path.reset();
            paint.reset();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3f);
            paint.setColor(graphs[i].getColor());
            double scale = (screenHeight - offset) * 1.0 / getMax();
            List<Integer> map = positions.get(i);
            path.moveTo(xCoordinatesLocal[0], screenHeight - offset - (int) (map.get(0) * scale));
            for (int j = 1; j < xCoordinatesLocal.length; j++)
                path.lineTo(xCoordinatesLocal[j], screenHeight - offset - (int) (map.get(j) * scale));
            canvas.drawPath(path, paint);
        }
    }

    private void drawTimeLine(Canvas canvas) {
        //todo make TextLineView
        paint.reset();
        paint.setAntiAlias(true);

        paint.setTextSize(Math.round(textSize * getResources().getDisplayMetrics().scaledDensity));
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getResources().getColor(R.color.rulerTextColor));

        int textCount = 6;
        float[] timeDistancePos = new float[textCount];
        float distance = right - left;
        for (int i = 0; i < textCount - 1; i++)
            timeDistancePos[i] = left + (i * (distance / (textCount - 1)));
        timeDistancePos[textCount - 1] = right;
        String[] times = chart.getFormatedTimes();
        String[] timeLine = new String[textCount];

        for (int i = 0; i < textCount; i++) {
            for (int j = 0; j < xCoordinatesAbsolute.length; j++) {
                if (xCoordinatesAbsolute[j] >= timeDistancePos[i]) {
                    timeLine[i] = times[j];
                    break;
                }
            }
        }

        //массив длин слов
        float[] textLengths = new float[textCount];
        for (int i = 0; i < textCount; i++)
            textLengths[i] = paint.measureText(timeLine[i]);

        //offset must be always more then text height
        Rect textBounds = new Rect();
        paint.getTextBounds(timeLine[0], 0, timeLine[0].length(), textBounds);
        float textOffset = (offset * 1f / 2) - (textBounds.height() * 1f / 2);
        //отрисовываем два крайних слова в начале и в конце
        canvas.drawText(timeLine[0], 0, screenHeight - textOffset, paint);
        float end = screenWidth - textLengths[textCount - 1];
        canvas.drawText(timeLine[textCount - 1], end, screenHeight - textOffset, paint);

        float begin = textLengths[0];
        float sum = 0;
        for (int i = 1; i < textCount - 1; i++)
            sum += textLengths[i];
        //todo think about: if the sum length of all words is more than being-end
        float space = (end - begin - sum) / (textCount - 1); //-2 слова и + 1 по формуле
        for (int i = 1; i < textCount - 1; i++) {
            begin += space;
            canvas.drawText(timeLine[i], begin, screenHeight - textOffset, paint);
            begin += textLengths[i];
        }
    }

    private void drawScale(Canvas canvas) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setTextSize(Math.round(textSize * getResources().getDisplayMetrics().scaledDensity));
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getResources().getColor(R.color.rulerTextColor));

        int step = (screenHeight - offset) / 6;
        int ruler = getMax() / 6;
        float[] textWidths;
        for (int i = 0; i < 6; i++) {
            String text = String.valueOf(ruler * i);
            textWidths = new float[text.length()];
            paint.getTextWidths(text, textWidths);
            canvas.drawText(text, 0, screenHeight - offset - (step * i) - 5, paint);
        }
    }

    private void drawLines(Canvas canvas) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1f);
        paint.setColor(getResources().getColor(R.color.rulerTextColor));
        int step = (screenHeight - offset) / 6;
        for (int i = 0; i < 6; i++) {
            canvas.drawLine(0, screenHeight - offset - (step * i), screenWidth, screenHeight - offset - (step * i), paint);
        }
    }
}
