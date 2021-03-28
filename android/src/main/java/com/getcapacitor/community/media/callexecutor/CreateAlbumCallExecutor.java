package com.getcapacitor.community.media.callexecutor;

import android.Manifest;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;

import java.io.File;

public class CreateAlbumCallExecutor extends CallExecutor {
    private static final String LOGTAG = "MediaPlugin/CallExec";

    public CreateAlbumCallExecutor(Plugin plugin, int requestCode) {
        super(plugin, requestCode, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public JSObject _execute(PluginCall call) throws Exception {
        return createAlbum(call);
    }

    private JSObject createAlbum(PluginCall call) throws Exception {
        String folderName = call.getString("name");
        Log.d(LOGTAG, "___CREATE ALBUM: " + String.valueOf(folderName));

        String folder;

        if (Build.VERSION.SDK_INT >= 29) {
            folder = plugin.getContext().getExternalMediaDirs()[0].getAbsolutePath()+"/"+folderName;
        } else {
            folder = Environment.getExternalStoragePublicDirectory(folderName).toString();
        }

        Log.d(LOGTAG, "New album folder: " + String.valueOf(folder));

        File f = new File(folder);

        if (!f.exists()) {
            if (!f.mkdir()) {
                Log.d(LOGTAG, "___ERROR ALBUM");
                throw new Exception ("Cannot create album: " + String.valueOf(folder));
            } else {
                Log.d(LOGTAG, "___SUCCESS ALBUM CREATED");
            }
        } else {
            Log.d(LOGTAG, "___SUCCESS ALBUM ALREADY EXISTS");
        }

        return null;
    }
}