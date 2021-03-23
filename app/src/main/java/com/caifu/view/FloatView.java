package com.caifu.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.caifu.R;
import com.caifu.service.ScreenService;

public class FloatView {

    private Activity mActivity;
    private ScreenService mScreenService;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private DragLayout mWindowView;
    private TextView mRecord;
    private View mClose;

    public FloatView(Activity activity, ScreenService screenService) {
        this.mActivity = activity;
        this.mScreenService = screenService;
        initWindowManger();
        View contentView = LayoutInflater.from(mActivity).inflate(R.layout.float_layout, null);
        mRecord = contentView.findViewById(R.id.record);
        mClose = contentView.findViewById(R.id.close);
        mRecord.setOnClickListener(v -> {
            if (mScreenService.isRunning()) {
                mScreenService.stopRecord();
            } else {
                mScreenService.startRecord();
            }
            mRecord.setText(mScreenService.isRunning() ? R.string.stop : R.string.record);
        });
        mClose.setOnClickListener(v -> {
            mScreenService.stopRecord();
            hide();
            activity.finish();
        });
        mWindowView.addView(contentView);
    }

    private void initWindowManger() {
        mWindowManager = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
        mParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mParams.format = PixelFormat.TRANSLUCENT;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        mParams.x = (int) mActivity.getResources().getDimension(R.dimen.float_view_margin);
        mParams.y = (int) mActivity.getResources().getDimension(R.dimen.float_view_margin);
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowView = new DragLayout(mActivity);
        mWindowView.bind(mWindowManager, mParams);
    }

    public void show() {
        if (!mWindowView.isAttachedToWindow()) {
            mWindowManager.addView(mWindowView, mParams);
        }
    }

    public void hide() {
        mWindowManager.removeView(mWindowView);
    }

}
