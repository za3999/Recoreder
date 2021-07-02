package com.caifu.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.caifu.util.MediaScanner;
import com.caifu.util.NotificationUtil;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ScreenService extends Service {
    private static final String rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            + File.separator + "ScreenRecord" + "/";
    private MediaRecorder mediaRecorder;
    private VirtualDisplay virtualDisplay;
    private boolean running;
    private int width = 720;
    private int height = 1080;
    private int dpi;
    private ImageReader mImageReader;
    private MediaProjection mediaProjection;
    private String filePath;
    private static HandlerThread recordHandlerThread = new HandlerThread("screen_thread");
    private static Handler recordHandler;

    static {
        recordHandlerThread.start();
        recordHandler = new Handler(recordHandlerThread.getLooper());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        NotificationUtil.startNotification(this);
        return new RecordBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        running = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        virtualDisplay.release();
        mediaProjection.stop();
        stopForeground(true);
        super.onDestroy();
    }


    public void setMediaProject(MediaProjection project) {
        mediaProjection = project;
    }


    public boolean isRunning() {
        return running;
    }


    public void setConfig(int width, int height, int dpi) {
        this.width = width;
        this.height = height;
        this.dpi = dpi;
    }


    /**
     * 开始录屏
     *
     * @return true
     */
    public boolean startRecord() {
        if (mediaProjection == null || running) {
            return false;
        }
        running = true;
        recordHandler.post(() -> {
            initRecorder();
            mediaRecorder.resume();
        });
        return true;
    }

    /**
     * 结束录屏
     *
     * @return true
     */
    public boolean stopRecord() {
        if (!running) {
            return false;
        }
        running = false;
        recordHandler.post(() -> {
            mediaRecorder.pause();
        });
        return true;
    }

    public void closeRecord() {
        recordHandler.post(() -> {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            new MediaScanner(ScreenService.this).setFilePaths(filePath).startScan();
        });
    }

    /**
     * 初始化ImageRead参数
     */
    @SuppressLint("WrongConstant")
    public void initImageReader() {
        if (mImageReader == null) {
            int maxImages = 2;
            mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, maxImages);
            createImageVirtualDisplay();
        }
    }

    /**
     * 创建一个录屏 Virtual
     */
    private void createVirtualDisplay() {
        virtualDisplay = mediaProjection
                .createVirtualDisplay("mediaprojection", width, height, dpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder
                        .getSurface(), null, null);
    }

    /**
     * 创建一个ImageReader Virtual
     */
    private void createImageVirtualDisplay() {
        virtualDisplay = mediaProjection
                .createVirtualDisplay("mediaprojection", width, height, dpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mImageReader
                        .getSurface(), null, null);
    }


    /**
     * 初始化保存屏幕录像的参数
     */
    private void initRecorder() {
        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            filePath = getSavePath() + System.currentTimeMillis() + ".mp4";
            mediaRecorder.setOutputFile(filePath);
            mediaRecorder.setVideoSize(width, height);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
            mediaRecorder.setVideoFrameRate(30);
            try {
                mediaRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            createVirtualDisplay();
            mediaRecorder.start();
        }
    }


    /**
     * 获取一个保存屏幕录像的路径
     *
     * @return path
     */
    public String getSavePath() {
        File file = new File(rootDir);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                return null;
            }
        }
        return rootDir;
    }


    /**
     * 请求完权限后马上获取有可能为null，可以通过判断is null来重复获取。
     */
    public Bitmap getBitmap() {
        Bitmap bitmap = cutoutFrame();
        if (bitmap == null) {
            getBitmap();
        }
        return bitmap;
    }

    /**
     * 通过底层来获取下一帧的图像
     *
     * @return bitmap
     */
    public Bitmap cutoutFrame() {
        Image image = mImageReader.acquireLatestImage();
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width +
                rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height);
    }

    public class RecordBinder extends Binder {
        public ScreenService getRecordService() {
            return ScreenService.this;
        }
    }
}
