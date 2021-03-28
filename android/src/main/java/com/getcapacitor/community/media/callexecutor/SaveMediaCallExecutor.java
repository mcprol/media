package com.getcapacitor.community.media.callexecutor;

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

public abstract class SaveMediaCallExecutor extends CallExecutor {
    private static final String LOGTAG = "MediaPlugin/CallExec";

    public SaveMediaCallExecutor(Plugin plugin, int requestCode, String permission) {
        super(plugin, requestCode, permission);
    }

    protected JSObject saveMedia(PluginCall call, String destination) throws Exception {
        Log.d(LOGTAG, "___SAVE MEDIA TO ALBUM: " + String.valueOf(destination));

        String inputPath = call.getString("path");
        if (inputPath == null) {
            throw new Exception("Required param: 'path'");
        }
        Log.d(LOGTAG, "inputPath: " + String.valueOf(inputPath));

        String album = call.getString("album");
        Log.d(LOGTAG, "album: " + String.valueOf(album));

        File albumDir = null;
        String albumPath;

        Log.d(LOGTAG,"SDK BUILD VERSION: " + String.valueOf(Build.VERSION.SDK_INT));
        if (Build.VERSION.SDK_INT >= 29) {
            albumPath = plugin.getContext().getExternalMediaDirs()[0].getAbsolutePath();
        } else {
            albumPath = Environment.getExternalStoragePublicDirectory(destination).getAbsolutePath();
        }
        Log.d(LOGTAG,"albumPath: " + String.valueOf(albumPath));

        // Log.d("ENV LOG", String.valueOf(getContext().getExternalMediaDirs()));

        if (album != null) {
            albumDir = new File(albumPath, album);
        } else{
            albumDir = new File(albumPath);
        }

        // if destination folder does not exist, create it
        Log.d(LOGTAG,"ALBUM DIR: " + String.valueOf(albumDir));
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
                targetFile = IOUtils.downloadFile(new URL(inputPath), albumDir);
                break;
            default:
                File inputFile = new File(inputUri.getPath());
                targetFile = IOUtils.copyFile(inputFile, albumDir);
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