package com.getcapacitor.community.media.callexecutor;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.media.IOUtils;

import java.io.File;
import java.net.URL;

public class SaveMediaCallExecutor extends AsyncCallExecutor {
    private String destination;

    public SaveMediaCallExecutor(Plugin plugin, int requestCode, String destination) {
        super(plugin, requestCode, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        this.destination = destination;
    }

    @Override
    public JSObject asyncExecute(PluginCall call) throws Exception {
        return saveMedia(call, destination);
    }

    protected JSObject saveMedia(PluginCall call, String destination) throws Exception {
        Log.d(LOG_TAG, "___SAVE MEDIA TO ALBUM: " + String.valueOf(destination));

        String inputPath = call.getString("path");
        if (inputPath == null) {
            throw new Exception("Required param: 'path'");
        }
        Log.d(LOG_TAG, "inputPath: " + String.valueOf(inputPath));

        String albumPath;

        Log.d(LOG_TAG,"SDK BUILD VERSION: " + String.valueOf(Build.VERSION.SDK_INT));
        if (Build.VERSION.SDK_INT >= 29) {
            File[] externalMediaDirs = plugin.getContext().getExternalMediaDirs();
            for (int i=0; i<externalMediaDirs.length; i++) {
                Log.d(LOG_TAG,"externalMediaDir[" + i + "]: " + String.valueOf(externalMediaDirs[i].getAbsolutePath()));
            }
            albumPath = externalMediaDirs[0].getAbsolutePath();
        } else {
            albumPath = Environment.getExternalStoragePublicDirectory(destination).getAbsolutePath();
        }
        Log.d(LOG_TAG,"albumPath: " + String.valueOf(albumPath));

        String album = call.getString("album");
        Log.d(LOG_TAG, "album: " + String.valueOf(album));

        File albumDir = null;
        if (album != null) {
            albumDir = new File(albumPath, album);
        } else {
            albumDir = new File(albumPath);
        }
        Log.d(LOG_TAG,"ALBUM DIR: " + String.valueOf(albumDir));

        // if destination folder does not exist, create it
        if (!albumDir.exists()) {
            if (!albumDir.mkdir()) {
                throw new Exception("Destination folder does not exist and cannot be created: " + String.valueOf(albumDir));
            }
        }

        Uri inputUri = Uri.parse(inputPath);
        File targetFile = null;
        switch (inputUri.getScheme()) {
            case "http":
            case "https":
                targetFile = new IOUtils().downloadFile(new URL(inputPath), albumDir);
                break;
            default:
                File inputFile = new File(inputUri.getPath());
                targetFile = new IOUtils().copyFile(inputFile, albumDir);
                break;
        }

        scanPhoto(targetFile);

        JSObject response = new JSObject();
        response.put("filePath", targetFile.toString());

        return response;
    }

    private void scanPhoto(File imageFile) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(imageFile);
        mediaScanIntent.setData(contentUri);
        plugin.getBridge().getActivity().sendBroadcast(mediaScanIntent);
    }
}