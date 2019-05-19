package ru.askhad.apishev.chartapp.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import ru.askhad.apishev.chartapp.R;

public class SliderView extends View {
    private int offsetHeight;
    private int screenWidth;
    private int screenHeight;
    private int mSliderColor;
    private int mSliderBackground;
    private float mDx;
    private boolean mDragMode;
    private boolean scaleToRight;
    private boolean scaleToLeft;
    private float x;
    private List<Update> moveableList;
    private Paint paint;
    private int mWidth;
    private float sliderX, sliderY;
    private int offset;


    public SliderView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public SliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public SliderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SliderView, defStyleAttr, 0);
        try {
            mSliderColor = array.getColor(R.styleable.SliderView_silderColor, 0);
            mSliderBackground = array.getColor(R.styleable.SliderView_silderBackground, 0);
        } finally {
            array.recycle();
        }
        sliderY = offsetHeight;
        offset = 15;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        moveableList = new ArrayList<>();
    }

    public void addObserver(Update moveable) {
        this.moveableList.add(moveable);
    }

    public void removeObserver(Update moveable) {
        this.moveableList.remove(moveable);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawSlider(canvas);
        drawWall(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        screenWidth = getMeasuredWidth() + getPaddingLeft() + getPaddingRight();
        screenHeight = getMeasuredHeight() + getPaddingBottom() + getPaddingTop();
        offsetHeight = (int) (screenHeight * 0.75);
        setWidth(getMeasuredWidth() / 6);
        initmX();
        for (Update m : moveableList) {
            m.update((int) getTopLeftCornerX(), (int) getTopRightCornerX());
        }
        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        //в результате движения возможна ситуация, когда при
        //изменении размера справа палец попадает на область перемещения картеки
        //поэтому були должны быть взаимоисключающими
        if (isGripped((int) e.getX())) {
            if (!getScaleToRight() && !getScaleToLeft())
                setInDragMode(true);
        } else if (scaleLeft((int) e.getX())) {
            if (!getScaleToRight() && !getInDragMode())
                setScaleToLeft(true);
        } else if (scaleRight((int) e.getX()))
            if (!getScaleToLeft() && !getInDragMode())
                setScaleToRight(true);
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //расстояние от точки касания до top-left каретки
                mDx = e.getX() - getTopLeftCornerX();
                //расстояние от точки касания до top-right каретки
                x = getTopRightCornerX() - e.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                //перемещение каретки
                float a = e.getX() + x;
                float value = e.getX() - mDx;
                if (mDragMode) {
                    //проверка выхода за левую границу
                    if (value < 1)
                        value = getOffset();
                    //проверка выхода за правую границу
                    if (screenWidth - a < 1)
                        value = screenWidth - getOffset() - getSliderWidth();
                    setSliderX(value);
                }
                //изменение размеров каретки справа
                if (scaleToRight) {
                    if (screenWidth - a < 3)
                        setSliderX(screenWidth - getOffset() - getSliderWidth() - 1);
                    else
                        setWidth((int) (e.getX() - getSliderX()));
                }
                //изменение размеров каретки слева
                if (scaleToLeft)
                    if (value < 5)
                        setSliderX(getOffset());
                    else
                        setLeftBorder(e.getX(), getSliderWidth() + (int) (getSliderX() - e.getX()));
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                //выключаем все були после открыва пальца от экрана
                setInDragMode(false);
                setScaleToLeft(false);
                setScaleToRight(false);
                break;
        }
        return true;
    }

    private void setInDragMode(boolean dragMode) {
        mDragMode = dragMode;
    }

    private boolean getInDragMode() {
        return mDragMode;
    }

    private void setScaleToRight(boolean value) {
        scaleToRight = value;
    }

    private boolean getScaleToRight() {
        return scaleToRight;
    }

    private void setScaleToLeft(boolean value) {
        scaleToLeft = value;
    }

    private boolean getScaleToLeft() {
        return scaleToLeft;
    }

    private void initmX() {
        sliderX = screenWidth - offset - mWidth;
    }

    public void setWidth(int w) {
        //минимальное значение для размера каретки
        if (w < 50)
            w = 50;
        mWidth = w;
        invalidate();
    }


    public float getTopLeftCornerX() {
        return sliderX - offset;
    }

    public float getTopRightCornerX() {
        return sliderX + mWidth + offset;
    }

    public void setLeftBorder(float mx, int w) {
        this.setSliderX(mx);
        this.setWidth(w);
        invalidate();
    }

    public int getSliderWidth() {
        return mWidth;
    }

    public float getSliderX() {
        return sliderX;
    }

    public void setSliderX(float mX) {
        sliderX = mX;
        invalidate();
    }

    public int getOffset() {
        return offset;
    }

    private boolean isGripped(int x) {
        return (x >= sliderX && x <= sliderX + mWidth);
    }

    private void drawWall(Canvas canvas) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.GRAY);
        paint.setAlpha(30);
        canvas.drawRect(0, 0, sliderX - offset, screenHeight, paint);
        canvas.drawRect(sliderX + mWidth + offset, 0, screenWidth, screenHeight, paint);
    }

    private void drawSlider(Canvas canvas) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getResources().getColor(R.color.sliderGrips));
        canvas.drawRect(sliderX - offset, sliderY, sliderX, screenHeight + sliderY, paint);
        canvas.drawRect(sliderX + mWidth, sliderY, sliderX + mWidth + offset, screenHeight + sliderY, paint);
        for (Update m : moveableList) {
            m.update((int) getTopLeftCornerX(), (int) getTopRightCornerX());
        }
    }

    private boolean scaleLeft(int x) {
        return (x >= sliderX - offset && x <= sliderX);
    }

    private boolean scaleRight(int x) {
        return (x >= sliderX + mWidth && x <= sliderX + mWidth + offset);
    }


}
