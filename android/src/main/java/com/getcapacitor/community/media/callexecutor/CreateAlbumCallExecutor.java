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

        String parentFolder = null;

        if (Build.VERSION.SDK_INT >= 29) {
            File[] externalMediaDirs = plugin.getContext().getExternalMediaDirs();
            for (int i=0; i<externalMediaDirs.length; i++) {
                File externalMediaDir = externalMediaDirs[i];
                if (externalMediaDir != null) {
                    Log.d(LOG_TAG,"externalMediaDir[" + i + "]: " + String.valueOf(externalMediaDir.getAbsolutePath()));
                    if (parentFolder == null) {
                        parentFolder = externalMediaDir.getAbsolutePath();
                    }
                } else {
                    Log.d(LOG_TAG,"externalMediaDir[" + i + "]: NULL");
                }
            }
        } else {
            String destination = Environment.DIRECTORY_PICTURES;
            parentFolder = Environment.getExternalStoragePublicDirectory(destination).toString();
        }

        if (parentFolder == null) {
            throw new Exception("Cannot find an 'ExternalMediaDir' available to create album: " + albumName);
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