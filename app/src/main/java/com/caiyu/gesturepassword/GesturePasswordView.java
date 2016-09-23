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
    private Paint mBitmapPaint;

    private Bitmap mBitmap;
    private Canvas mCanvas;

    private Path mPath;
    
    private List<Integer> inputList = new ArrayList<>();

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

        mBitmapPaint = new Paint();

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
                reset();
            case MotionEvent.ACTION_MOVE:
                if (!inputList.isEmpty()) {
                    int selectedIndex = getSelectedPoint(event.getX(), event.getY());
                    if (selectedIndex == -1) {
                        drawNewLine(event.getX(), event.getY());
                    }
                    else {  //触摸到某个点
                        if (selectedIndex != inputList.get(inputList.size() - 1)) {
                            drawNewLine(X[selectedIndex], Y[selectedIndex]);
                            mPath = new Path();
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
        mPath.reset();
        mCanvas.drawPath(mPath, linePaint);
        invalidate();
    }

    private void drawNewLine(float eventX, float eventY) {
        float centerX = X[inputList.get(inputList.size() - 1)];
        float centerY = Y[inputList.get(inputList.size() - 1)];
        mPath.reset();
        mPath.moveTo(centerX, centerY);
        mPath.lineTo(eventX, eventY);
        mCanvas.drawPath(mPath, linePaint);
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
