package com.caiyu.gesturepassword;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

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

    private Paint linePaint;
    private Paint whitePointPaint;
    private Paint redPointPaint;

    private Path mPath;
    
    private List<Integer> inputList = new ArrayList<>();

    private float curEventX = -1;
    private float curEventY = -1;

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

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(COLOR_RED);
        linePaint.setStrokeWidth(Util.dp2px(getContext(), 2));

        whitePointPaint = new Paint();
        whitePointPaint.setAntiAlias(true);
        whitePointPaint.setColor(COLOR_WHITE);

        redPointPaint = new Paint();
        redPointPaint.setAntiAlias(true);
        redPointPaint.setColor(COLOR_RED);

        mPath = new Path();
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
    }

    @Override
    public void onDraw(Canvas canvas) {
        drawPoints(canvas);
        drawPath(canvas);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                reset();
            case MotionEvent.ACTION_MOVE:
                if (!inputList.isEmpty()) {
                    int selectedIndex = getSelectedPoint(event.getX(), event.getY());
                    if (selectedIndex == -1) {
                        curEventX = event.getX();
                        curEventY = event.getY();
                        invalidate();
                    }
                    else {  //触摸到某个点
                        if (selectedIndex != inputList.get(inputList.size() - 1)) {
                            inputList.add(selectedIndex);
                            curEventX = curEventY = -1;
                            invalidate();
                        }
                    }
                }
                else {
                    int selectedIndex = getSelectedPoint(event.getX(), event.getY());
                    if (selectedIndex != -1) {
                        inputList.add(selectedIndex);
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

    private void drawPoints(Canvas canvas) {
        for (int i = 0; i < POINT_COUNT; i++) {
            Paint paint;
            if (inputList.contains(i)) {
                paint = redPointPaint;
            } else {
                paint = whitePointPaint;
            }
            canvas.drawCircle(X[i], Y[i], POINT_RADIUS, paint);
        }
    }

    private void drawPath(Canvas canvas) {
        mPath.reset();
        for (int i = 0; i < inputList.size(); i++) {
            if (i == 0) {
                mPath.moveTo(X[inputList.get(i)], Y[inputList.get(i)]);
            }
            else {
                mPath.lineTo(X[inputList.get(i)], Y[inputList.get(i)]);
            }
        }
        if (curEventX != -1 && curEventY != -1) {
            mPath.lineTo(curEventX, curEventY);
        }
        canvas.drawPath(mPath, linePaint);
    }

    public void reset() {
        inputList.clear();
        curEventX = curEventY = -1;
        invalidate();
    }

    public void setOnCompleteListener(OnCompleteListener listener) {
        mOnCompleteListener = listener;
    }

    private void clearOldLine() {
        curEventX = curEventY = -1;
        invalidate();
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
