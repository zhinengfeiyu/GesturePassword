package com.caiyu.gesturepassword;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 渝 on 2016/9/23.
 */

public class GesturePasswordView extends View {

    private static final int COLOR_WHITE = 0xffffffff;
    private static final int COLOR_RED = 0xffff0000;
    private static final int POINT_COUNT = 9;
    private int POINT_RADIUS;
    private int TOTAL_WIDTH;

    private int[] X;
    private int[] Y;

    private Paint clearLinePaint;
    private Paint linePaint;
    private Paint whitePointPaint;
    private Paint redPointPaint;
    private Paint mBitmapPaint;

    private Bitmap mBitmap;
    private Canvas mCanvas;
    
    private List<Integer> inputList = new ArrayList<>();
    private float lastEventX;
    private float lastEventY;
    private float lastStartX = -1;
    private float lastStartY = -1;

    private OnCompleteListener mOnCompleteListener;

    public GesturePasswordView(Context context) {
        super(context);
        init();
    }

    public GesturePasswordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        POINT_RADIUS = Util.dp2px(getContext(), 15);

        clearLinePaint = new Paint();
        //clearLinePaint.setAntiAlias(true);
        clearLinePaint.setStrokeWidth(Util.dp2px(getContext(), 2));
        clearLinePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        linePaint = new Paint();
        //linePaint.setAntiAlias(true);
        linePaint.setColor(COLOR_RED);
        linePaint.setStrokeWidth(Util.dp2px(getContext(), 2));

        whitePointPaint = new Paint();
        whitePointPaint.setAntiAlias(true);
        whitePointPaint.setColor(COLOR_WHITE);

        redPointPaint = new Paint();
        redPointPaint.setAntiAlias(true);
        redPointPaint.setColor(COLOR_RED);

        mBitmapPaint = new Paint();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        int resultWidth = widthSpecSize;
        int resultHeight = heightSpecSize;

        if (widthSpecMode == MeasureSpec.AT_MOST) {
            resultWidth = Util.dp2px(getContext(), 250);
        }
        if (heightSpecMode == MeasureSpec.AT_MOST) {
            resultHeight = Util.dp2px(getContext(), 250);
        }

        setMeasuredDimension(resultWidth, resultHeight);
    }

    @Override
    public void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight) {
        TOTAL_WIDTH = Math.min(newWidth, newHeight);
        X = new int[] {
                POINT_RADIUS, TOTAL_WIDTH / 2, TOTAL_WIDTH - POINT_RADIUS,
                POINT_RADIUS, TOTAL_WIDTH / 2, TOTAL_WIDTH - POINT_RADIUS,
                POINT_RADIUS, TOTAL_WIDTH / 2, TOTAL_WIDTH - POINT_RADIUS
        };
        Y = new int[] {
                POINT_RADIUS, POINT_RADIUS, POINT_RADIUS,
                TOTAL_WIDTH / 2, TOTAL_WIDTH / 2, TOTAL_WIDTH / 2,
                TOTAL_WIDTH - POINT_RADIUS, TOTAL_WIDTH - POINT_RADIUS, TOTAL_WIDTH - POINT_RADIUS
        };
        initView();
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (!inputList.isEmpty()) {
                    int selectedIndex = getSelectedPoint(event.getX(), event.getY());
                    if (selectedIndex == -1) {
                        clearOldLine();
                        drawNewLine(event.getX(), event.getY());
                    }
                    else {
                        if (selectedIndex != inputList.get(inputList.size() - 1)) {
                            clearOldLine();
                            mCanvas.drawLine(
                                    X[inputList.get(inputList.size() - 1)],
                                    Y[inputList.get(inputList.size() - 1)],
                                    X[selectedIndex],
                                    Y[selectedIndex],
                                    linePaint);
                            invalidate();
                            lastStartX = lastStartY = -1;
                            if (!inputList.contains(selectedIndex)) {
                                mCanvas.drawCircle(X[selectedIndex], Y[selectedIndex], POINT_RADIUS, redPointPaint);
                                invalidate();
                            }
                            inputList.add(selectedIndex);
                        }
                    }
                }
                else {
                    int selectedIndex = getSelectedPoint(event.getX(), event.getY());
                    if (selectedIndex != -1) {
                        inputList.add(selectedIndex);
                        mCanvas.drawCircle(X[selectedIndex], Y[selectedIndex], POINT_RADIUS, redPointPaint);
                        invalidate();
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                clearOldLine();
                if (inputList.size() >= 2) {
                    mOnCompleteListener.onComplete(inputList);
                }
                else if (inputList.size() == 1) {
                    reset();
                }
                break;
        }
        return true;
    }

    private void initView() {
        inputList.clear();
        
        mBitmap = Bitmap.createBitmap(TOTAL_WIDTH, TOTAL_WIDTH, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        for (int i = 0; i < POINT_COUNT; i++) {
            mCanvas.drawCircle(X[i], Y[i], POINT_RADIUS, whitePointPaint);
        }
    }

    public void reset() {
        initView();
        invalidate();
    }

    public void setOnCompleteListener(OnCompleteListener listener) {
        mOnCompleteListener = listener;
    }

    private void clearOldLine() {
        if (lastStartX != -1 && lastStartY != -1) {
            mCanvas.drawLine(lastStartX, lastStartY, lastEventX, lastEventY, clearLinePaint);
            invalidate();
        }
    }

    private void drawNewLine(float eventX, float eventY) {
        float centerX = X[inputList.get(inputList.size() - 1)];
        float centerY = Y[inputList.get(inputList.size() - 1)];
        double newInstance = Math.sqrt(
                (eventX - centerX) * (eventX - centerX)
                + (eventY - centerY) * (eventY - centerY));
        double sin = (eventY - centerY) / newInstance;
        double cos = (eventX - centerX) / newInstance;
        float newStartX = centerX + POINT_RADIUS * (float) cos;
        float newStartY = centerY + POINT_RADIUS * (float) sin;
        mCanvas.drawLine(newStartX, newStartY, eventX, eventY, linePaint);
        invalidate();
        lastStartX = newStartX;
        lastStartY = newStartY;
        lastEventX = eventX;
        lastEventY = eventY;
    }

    private int getSelectedPoint(float eventX, float eventY) {
        for (int i = 0; i < POINT_COUNT; i++) {
            if (isInArea(eventX, eventY, X[i], Y[i])) {
                return i;
            }
        }
        return -1;
    }

    private boolean isInArea(float eventX, float eventY, float targetX, float targetY) {
        return (eventX - targetX) * (eventX - targetX) + (eventY - targetY) * (eventY - targetY)
                < POINT_RADIUS * POINT_RADIUS;
    }

    public interface OnCompleteListener {
        void onComplete(List<Integer> result);
    }
}
