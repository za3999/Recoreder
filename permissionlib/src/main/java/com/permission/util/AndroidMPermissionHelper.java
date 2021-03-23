package com.permission.util; /**
 *
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.cf.common.permission.PermissionCallBack;
import com.permission.R;

import java.util.ArrayList;

/**
 * 权限管理Helper类
 *
 * @author zhengcf on 2017/7/18.
 */
public final class AndroidMPermissionHelper {

    public static final int REQUEST_CODE_ASK_PERMISSIONS = 1;

    public static final String SECURITY_RESULT_ACTION = "security_result_action";


    /**
     * 带回调的权限检查
     *
     * @param context
     * @param callBack
     * @param permissions
     */
    public static void checkPermission(Context context, PermissionCallBack callBack, String... permissions) {
        if (permissions == null || permissions.length == 0) {
            throw new RuntimeException("permissions is null");
        }

        if (isAllPermissionGranted(context, permissions)) {
            if (callBack != null) {
                callBack.onGranted();
            } else {
                Toast.makeText(context, context.getString(R.string.str_get_permission_success), Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                if (callBack != null) {
                    callBack.onDenied();
                } else {
                    Toast.makeText(context, context.getString(R.string.str_get_permission_fail), Toast.LENGTH_SHORT).show();
                }
            } else {
                showPermissionActivity(context, callBack, getNotGrantedPermission(context, permissions));
            }
        }
    }

    /**
     * 获取没有被授予的权限的列表
     *
     * @param permissions
     * @return
     */

    public static ArrayList<String> getNotGrantedPermission(Context context, String[] permissions, int[] grantResults) {
        ArrayList<String> permissionList = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (permissions[i].equals(Constant.PERMISSION_CAMERA)) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED || !isCameraReady()) {
                    permissionList.add(permissions[i]);
                }
            } else if (permissions[i].equals(Constant.PERMISSION_MICROPHONE)) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED || !isRecorderReady(context)) {
                    permissionList.add(permissions[i]);
                }
            } else if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permissions[i]);
            }
        }
        return permissionList;
    }


    /**
     * 检查是否所有权限都通过
     *
     * @param context
     * @param permissions
     * @return
     */
    public static boolean isAllPermissionGranted(Context context, String... permissions) {
        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(Constant.PERMISSION_CAMERA)) {
                if (ActivityCompat.checkSelfPermission(context, permissions[i]) != PackageManager.PERMISSION_GRANTED || !isCameraReady()) {
                    return false;
                }
            } else if (permissions[i].equals(Constant.PERMISSION_MICROPHONE)) {
                if (ActivityCompat.checkSelfPermission(context, permissions[i]) != PackageManager.PERMISSION_GRANTED || !isRecorderReady(context)) {
                    return false;
                }
            } else if (ActivityCompat.checkSelfPermission(context, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查权限返回结果
     *
     * @param grantResults
     * @return
     */
    public static boolean isAllPermissionGranted(Context context, String[] permissions, int[] grantResults) {
        for (int i = 0; i < grantResults.length; i++) {
            if (permissions[i].equals(Constant.PERMISSION_CAMERA)) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED || !isCameraReady()) {
                    return false;
                }
            } else if (permissions[i].equals(Constant.PERMISSION_MICROPHONE)) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED || !isRecorderReady(context)) {
                    return false;
                }
            } else if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 显示权限框
     *
     * @param context
     * @param callback
     * @param permissions
     */
    private static void showPermissionActivity(Context context, final PermissionCallBack callback,
                                               String... permissions) {
        final long code = System.currentTimeMillis();
        Intent intent = new Intent(context, AndroidMPermissionActivity.class);
        intent.putExtra("permissions", permissions);
        intent.putExtra("code", code);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final LocalBroadcastManager mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
        mLocalBroadcastManager.registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getLongExtra("code", 0l) == code) {
                    if (callback != null) {
                        if (intent.getBooleanExtra("success", false)) {
                            callback.onGranted();
                        } else {
                            callback.onDenied();
                        }
                    }
                    mLocalBroadcastManager.unregisterReceiver(this);
                }
            }
        }, new IntentFilter(SECURITY_RESULT_ACTION));
        context.startActivity(intent);
    }

    /**
     * 获取没有权限的列表
     *
     * @param context
     * @param permissions
     * @return
     */
    @SuppressWarnings("unused")
    private static String[] getDeniedPermission(Context context, String... permissions) {
        ArrayList<String> permissionList = new ArrayList<String>();
        for (int i = 0; i < permissions.length; i++) {
            if (ActivityCompat.checkSelfPermission(context, permissions[i]) == PackageManager.PERMISSION_DENIED) {
                permissionList.add(permissions[i]);
            }
        }
        return permissionList.toArray(new String[permissionList.size()]);
    }

    /**
     * 获取没有被授予的权限的列表
     *
     * @param context
     * @param permissions
     * @return
     */

    private static String[] getNotGrantedPermission(Context context, String... permissions) {
        ArrayList<String> permissionList = new ArrayList<String>();
        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(Constant.PERMISSION_CAMERA)) {
                if (ActivityCompat.checkSelfPermission(context, permissions[i]) != PackageManager.PERMISSION_GRANTED || !isCameraReady()) {
                    permissionList.add(permissions[i]);
                }
            } else if (permissions[i].equals(Constant.PERMISSION_MICROPHONE)) {
                if (ActivityCompat.checkSelfPermission(context, permissions[i]) != PackageManager.PERMISSION_GRANTED || !isRecorderReady(context)) {
                    permissionList.add(permissions[i]);
                }
            } else if (ActivityCompat.checkSelfPermission(context, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permissions[i]);
            }
        }
        return permissionList.toArray(new String[permissionList.size()]);
    }


    public static boolean isCameraReady() {
        return true;
//        boolean isCameraReady = false;
//        Camera camera = null;
//        try {
//            camera = Camera.open();
//            Camera.Parameters mParameters = camera.getParameters();
//            camera.setParameters(mParameters);
//            isCameraReady = true;
//        } catch (Exception e) {
//        } finally {
//            if (camera != null) {
//                camera.release();
//            }
//        }
//
//        return isCameraReady;
    }

    public static boolean isRecorderReady(Context context) {
        boolean isRecorderReady = true;
        return isRecorderReady;
//        MediaRecorder mediaRecorder = null;
//        File file = null;
//        try {
//            mediaRecorder = new MediaRecorder();
//            mediaRecorder.reset();
//            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
//            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//            mediaRecorder.setOutputFile(context.getFilesDir().getAbsolutePath());
//            mediaRecorder.prepare();
//        } catch (Exception e) {
//            isRecorderReady = false;
//        }
//        if (mediaRecorder != null) {
//            try {
//                mediaRecorder.release();
//                if (file != null && file.exists()) {
//                    file.deleteOnExit();
//                }
//            } catch (Exception e) {
//                return isRecorderReady;
//            }
//        }
//        return isRecorderReady;
    }
}
