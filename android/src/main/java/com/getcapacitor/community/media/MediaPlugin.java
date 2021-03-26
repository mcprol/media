package com.getcapacitor.community.media;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Date;


@NativePlugin(permissions = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.INTERNET
})
public class MediaPlugin extends Plugin {

    private static final String LOGTAG = "MediaPlugin";

    // @todo
    @PluginMethod()
    public void getMedias(PluginCall call) {
        call.unimplemented();
    }

    @PluginMethod()
    public void getAlbums(PluginCall call) {
        Log.d(LOGTAG, "GET ALBUMS");
        if (hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Log.d(LOGTAG, "HAS PERMISSION");
            _getAlbums(call);
        } else {
            Log.d(LOGTAG, "NOT ALLOWED");
            saveCall(call);
            pluginRequestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, 1986);
        }
    }

    private void _getAlbums(PluginCall call) {
        Log.d(LOGTAG, "___GET ALBUMS");

        JSObject response = new JSObject();
        JSArray albums = new JSArray();
        StringBuffer list = new StringBuffer();

        String[] projection = new String[]{"DISTINCT " + MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME};
        Cursor cur = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);

        while (cur.moveToNext()) {
            String albumName = cur.getString((cur.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)));
            JSObject album = new JSObject();

            list.append(albumName + "\n");

            album.put("name", albumName);
            albums.put(album);
        }

        response.put("albums", albums);
        Log.d(LOGTAG, String.valueOf(response));
        Log.d(LOGTAG, "___GET ALBUMS FINISHED");

        call.resolve(response);
    }


    @PluginMethod()
    public void getPhotos(PluginCall call) {
        call.unimplemented();
    }

    @PluginMethod()
    public void createAlbum(PluginCall call) {
        Log.d(LOGTAG, "CREATE ALBUM");
        if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.d(LOGTAG, "HAS PERMISSION");
            _createAlbum(call);
        } else {
            Log.d(LOGTAG, "NOT ALLOWED");
            saveCall(call);
            pluginRequestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 1986);
        }
    }

    private void _createAlbum(PluginCall call) {
        String folderName = call.getString("name");
        Log.d(LOGTAG, "___CREATE ALBUM: " + String.valueOf(folderName));

        String folder;

        if (Build.VERSION.SDK_INT >= 29) {
            folder = getContext().getExternalMediaDirs()[0].getAbsolutePath()+"/"+folderName;
        }else{
            folder = Environment.getExternalStoragePublicDirectory(folderName).toString();
        }

        Log.d(LOGTAG, "New album folder: " + String.valueOf(folder));

        File f = new File(folder);

        if (!f.exists()) {
            if (!f.mkdir()) {
                Log.d(LOGTAG, "___ERROR ALBUM");
                call.error("Cant create album");
            } else {
                Log.d(LOGTAG, "___SUCCESS ALBUM CREATED");
                call.success();
            }
        } else {
            Log.d(LOGTAG, "___SUCCESS ALBUM ALREADY EXISTS");
            call.success();
        }

    }


    @PluginMethod()
    public void savePhoto(PluginCall call) {
        Log.d(LOGTAG, "SAVE PHOTO TO ALBUM");
        if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.d(LOGTAG, "HAS PERMISSION");
            _saveMedia(call, "PICTURES");
        } else {
            Log.d(LOGTAG, "NOT ALLOWED");
            saveCall(call);
            pluginRequestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 1986);
            Log.d(LOGTAG, "___SAVE PHOTO TO ALBUM AFTER PERMISSION REQUEST");
        }
    }

    @PluginMethod()
    public void saveVideo(PluginCall call) {
        Log.d(LOGTAG, "SAVE VIDEO TO ALBUM");
        if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.d(LOGTAG, "HAS PERMISSION");
            _saveMedia(call, "MOVIES");
        } else {
            Log.d(LOGTAG, "NOT ALLOWED");
            saveCall(call);
            pluginRequestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 1986);
        }
    }


    @PluginMethod()
    public void saveGif(PluginCall call) {
        Log.d(LOGTAG, "SAVE GIF TO ALBUM");
        if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.d(LOGTAG, "HAS PERMISSION");
            _saveMedia(call, "PICTURES");
        } else {
            Log.d(LOGTAG, "NOT ALLOWED");
            saveCall(call);
            pluginRequestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 1986);
        }
    }


    private void _saveMedia(PluginCall call, String destination) {
        Log.d(LOGTAG, "___SAVE MEDIA TO ALBUM: " + String.valueOf(destination));
        String dest;
        if ("MOVIES".equalsIgnoreCase(destination)) {
            dest = Environment.DIRECTORY_MOVIES;
        } else {
            dest = Environment.DIRECTORY_PICTURES;
        }

        String inputPath = call.getString("path");
        if (inputPath == null) {
            call.reject("Input file path is required");
            return;
        }
        Log.d(LOGTAG, "inputPath: " + String.valueOf(inputPath));

        String album = call.getString("album");
        Log.d(LOGTAG, "album: " + String.valueOf(album));

        File albumDir = null;
        String albumPath;

        Log.d(LOGTAG,"SDK BUILD VERSION: " + String.valueOf(Build.VERSION.SDK_INT));
        if (Build.VERSION.SDK_INT >= 29) {
            albumPath = getContext().getExternalMediaDirs()[0].getAbsolutePath();

        } else{
            albumPath = Environment.getExternalStoragePublicDirectory(dest).getAbsolutePath();
        }
        Log.d(LOGTAG,"albumPath: " + String.valueOf(albumPath));

        // Log.d("ENV LOG", String.valueOf(getContext().getExternalMediaDirs()));

        if (album != null) {
            albumDir = new File(albumPath, album);
        } else{
            albumDir = new File(albumPath);
        }

        Log.d(LOGTAG,"ALBUM DIR: " + String.valueOf(albumDir));
        // if destination folder does not exist, create it
        if (!albumDir.exists()) {
            if (!albumDir.mkdir()) {
                throw new RuntimeException("Destination folder does not exist and cannot be created.");
            }
        }

        try {
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

            JSObject result = new JSObject();
            result.put("filePath", targetFile.toString());
            call.resolve(result);

        } catch (Exception e) {
            call.reject("Exception occurred", e);
        }

    }



    private void scanPhoto(File imageFile) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(imageFile);
        mediaScanIntent.setData(contentUri);
        bridge.getActivity().sendBroadcast(mediaScanIntent);
    }

    @PluginMethod()
    public void hasStoragePermission(PluginCall call) {
        if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            call.success();
        } else {
            call.error("permission denied WRITE_EXTERNAL_STORAGE");
        }
    }

    @PluginMethod()
    public void requestStoragePermission(PluginCall call) {
        pluginRequestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 1986);
        if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            call.success();
        }else{
            call.error("permission denied");
        }
    }

}
