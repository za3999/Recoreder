package com.caifu.util;

import android.content.ContentValues;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class AlbumUtil {

    public static void insertFileToMediaStore(Context context, String filePath) {
        Uri uri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, getCommonContentValues(filePath));
//        insertFileContent(context, filePath, uri);
    }

    private static void insertFileContent(Context context, String filePath, Uri uri) {
        new Thread(() -> {
            try {
                InputStream inputStream = new FileInputStream(filePath);
                if (uri != null && inputStream != null) {
                    OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                    byte[] inputData = new byte[1024];
                    int len = bufferedInputStream.read(inputData);
                    while (len >= 0) {
                        bufferedOutputStream.write(inputData, 0, len);
                        bufferedOutputStream.flush();
                        len = bufferedInputStream.read(inputData);
                    }
                    outputStream.close();
                }
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static ContentValues getCommonContentValues(String filePath) {
        ContentValues values = new ContentValues();
        long time = System.currentTimeMillis();
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, time / 1000);
        values.put(MediaStore.MediaColumns.DATE_ADDED, time / 1000);
        values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, time);
        values.put(MediaStore.Video.Media.DATA, filePath);
        values.put(MediaStore.Images.Media.MIME_TYPE, getVideoMimeType(filePath));

        MediaMetadataRetriever rmr = new MediaMetadataRetriever();
        rmr.setDataSource(filePath);
        int outWidth = Integer.parseInt(rmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        int outHeight = Integer.parseInt(rmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            if (outWidth > 0) values.put(MediaStore.Images.ImageColumns.WIDTH, outWidth);
            if (outHeight > 0) values.put(MediaStore.Images.ImageColumns.HEIGHT, outHeight);
        }
        long duration = 0;
        String durationStr = rmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        if (!TextUtils.isEmpty(durationStr)) {
            duration = Long.parseLong(durationStr);
        }
        values.put(MediaStore.Video.VideoColumns.DURATION, duration);

        File saveFile = new File(filePath);
        values.put(MediaStore.MediaColumns.TITLE, saveFile.getName());
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, saveFile.getName());
        return values;
    }

    private static String getVideoMimeType(String path) {
        String lowerPath = path.toLowerCase();
        if (lowerPath.endsWith("mp4") || lowerPath.endsWith("mpeg4")) {
            return "video/mp4";
        } else if (lowerPath.endsWith("3gp")) {
            return "video/3gp";
        }
        return "video/mp4";
    }
}
