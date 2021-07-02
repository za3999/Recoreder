package com.caifu.util;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

public class MediaScanner {

    private MediaScannerConnection mediaScanConn;
    private String[] filePaths;
    private String[] mimeTypes;

    public MediaScanner(Context context) {
        this.mediaScanConn = new MediaScannerConnection(context, new MediaScannerConnection.MediaScannerConnectionClient() {
            @Override
            public void onMediaScannerConnected() {
                mediaScanConn.scanFile(context, filePaths, mimeTypes, (path, uri) -> {
                    Log.d("MediaScanner", "scanCompleted path:" + path + ",uri:" + uri);
                });
            }

            @Override
            public void onScanCompleted(String path, Uri uri) {
                mediaScanConn.disconnect();
            }
        });
    }

    public MediaScanner setFilePaths(String... filePaths) {
        this.filePaths = filePaths;
        return this;
    }

    public MediaScanner setMimeTypes(String... mimeTypes) {
        this.mimeTypes = mimeTypes;
        return this;
    }

    public void startScan() {
        mediaScanConn.connect();
    }
}
