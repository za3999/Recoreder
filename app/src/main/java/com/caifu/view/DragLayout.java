package com.caifu.view;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DragLayout extends FrameLayout {

    protected PointF mLastPoint = new PointF();
    private float mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager mWindowManager;
    private int width;
    private int height;

    public DragLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public DragLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
    }

    public void bind(WindowManager windowManager, WindowManager.LayoutParams layoutParams) {
        this.mWindowManager = windowManager;
        this.mLayoutParams = layoutParams;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastPoint.set(ev.getRawX(), ev.getRawY());
                break;
            case MotionEvent.ACTION_MOVE:
                if (needIntercept(ev)) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mLayoutParams.x += (int) (event.getRawX() - mLastPoint.x);
                mLayoutParams.y += (int) (event.getRawY() - mLastPoint.y);
                checkPoint();
                mWindowManager.updateViewLayout(this, mLayoutParams);
                mLastPoint.set(event.getRawX(), event.getRawY());
                break;
        }
        return super.onTouchEvent(event);
    }

    private void checkPoint() {
        mLayoutParams.x = mLayoutParams.x < 0 ? 0 : mLayoutParams.x;
        if (mLayoutParams.x + getWidth() > width) {
            mLayoutParams.x = width - getWidth();
        }
        mLayoutParams.y = mLayoutParams.y < 0 ? 0 : mLayoutParams.y;
        if (mLayoutParams.y + getHeight() > height) {
            mLayoutParams.y = height - getHeight();
        }
    }

    private boolean needIntercept(MotionEvent event) {
        return Math.abs(event.getRawX() - mLastPoint.x) >= mTouchSlop || Math.abs(event.getRawY() - mLastPoint.y) >= mTouchSlop;
    }
}
