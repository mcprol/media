package com.getcapacitor.community.media.callexecutor;

import android.Manifest;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;

import java.io.File;

public class CreateAlbumCallExecutor extends SyncCallExecutor {

    public CreateAlbumCallExecutor(Plugin plugin, int requestCode) {
        super(plugin, requestCode, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public JSObject syncExecute(PluginCall call) throws Exception {
        return createAlbum(call);
    }

    private JSObject createAlbum(PluginCall call) throws Exception {

        String albumName = call.getString("name");
        Log.d(LOG_TAG, "___CREATE ALBUM: " + String.valueOf(albumName));

        String parentFolder;

        if (Build.VERSION.SDK_INT >= 29) {
            parentFolder = plugin.getContext().getExternalMediaDirs()[0].getAbsolutePath();
        } else {
            String destination = Environment.DIRECTORY_PICTURES;
            parentFolder = Environment.getExternalStoragePublicDirectory(destination).toString();
        }

        String folder = parentFolder  + "/" + albumName;
        Log.d(LOG_TAG, "New album folder: " + String.valueOf(folder));

        File f = new File(folder);

        if (!f.exists()) {
            if (!f.mkdir()) {
                Log.d(LOG_TAG, "___ERROR ALBUM");
                throw new Exception ("Cannot create album: " + String.valueOf(folder));
            } else {
                Log.d(LOG_TAG, "___SUCCESS ALBUM CREATED");
            }
        } else {
            Log.d(LOG_TAG, "___SUCCESS ALBUM ALREADY EXISTS");
        }

        return null;
    }
}