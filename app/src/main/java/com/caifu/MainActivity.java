package com.caifu;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.caifu.service.ScreenService;
import com.caifu.view.FloatView;
import com.cf.common.ServiceManager;
import com.cf.common.permission.IPermissionService;
import com.cf.common.permission.PermissionCallBack;
import com.permission.util.Constant;

public class MainActivity extends AppCompatActivity {

    private static final int RECORD_RECORD_CODE = 10086;
    private static final int RECORD_OVER_VIEW_CODE = 10010;

    ScreenService screenService;
    MediaProjectionManager mediaProjectionManager;
    MediaProjection mediaProjection;
    FloatView mFloatView;

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            ScreenService.RecordBinder binder = (ScreenService.RecordBinder) service;
            screenService = binder.getRecordService();
            screenService.setConfig(displayMetrics.widthPixels, displayMetrics.heightPixels, displayMetrics.densityDpi);
            mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            Intent screenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent();
            startActivityForResult(screenCaptureIntent, RECORD_RECORD_CODE);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IPermissionService permissionService = ServiceManager.getServices(IPermissionService.class);
        permissionService.checkPermission(this, new PermissionCallBack() {
                    @Override
                    public void onGranted() {
                    }

                    @Override
                    public void onDenied() {
                        Toast.makeText(MainActivity.this, "权限请求失败", Toast.LENGTH_SHORT).show();
                    }
                }, Constant.PERMISSION_STORAGE,
                Constant.PERMISSION_READ_STORAGE,
                Constant.PERMISSION_MICROPHONE);
    }

    private void openPermissionActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivityForResult(intent, 1);
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECORD_RECORD_CODE && resultCode == RESULT_OK) {
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
            screenService.setMediaProject(mediaProjection);
            screenService.initImageReader();
            startRecord();
        } else if (requestCode == RECORD_OVER_VIEW_CODE && resultCode == RESULT_OK) {
            startRecord();
        }
    }

    public void start(View view) {
        startRecord();
    }

    private void startRecord() {
        if (!Settings.canDrawOverlays(this)) {
            openPermissionActivity();
            return;
        }
        if (screenService == null) {
            bindService(new Intent(MainActivity.this, ScreenService.class), serviceConnection, Context.BIND_AUTO_CREATE);
            return;
        }
        if (screenService != null) {
            if (mFloatView == null) {
                mFloatView = new FloatView(this, screenService);
            }
            mFloatView.show();
            moveTaskToBack(false);
        }
    }
}