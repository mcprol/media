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
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;


@NativePlugin(permissions = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
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

        Uri inputUri = Uri.parse(inputPath);
        File inputFile = new File(inputUri.getPath());

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

        try {
            File expFile = copyFile(inputFile, albumDir);
            scanPhoto(expFile);

            JSObject result = new JSObject();
            result.put("filePath", expFile.toString());
            call.resolve(result);

        } catch (RuntimeException e) {
            call.reject("RuntimeException occurred", e);
        }

    }

    private File copyFile(File inputFile, File albumDir) {

        // if destination folder does not exist, create it
        if (!albumDir.exists()) {
            if (!albumDir.mkdir()) {
                throw new RuntimeException("Destination folder does not exist and cannot be created.");
            }
        }

        String absolutePath = inputFile.getAbsolutePath();
        String extension = absolutePath.substring(absolutePath.lastIndexOf("."));

        // generate image file name using current date and time
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        File newFile = new File(albumDir, "IMG_" + timeStamp + extension);

        // Read and write image files
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            inChannel = new FileInputStream(inputFile).getChannel();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Source file not found: " + String.valueOf(inputFile) + ", error: " + e.getMessage(), e);
        }
        try {
            outChannel = new FileOutputStream(newFile).getChannel();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Copy file not found: " + String.valueOf(newFile) + ", error: " + e.getMessage(), e);
        }

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            throw new RuntimeException("Error transfering file, error: " + e.getMessage(), e);
        } finally {
            if (inChannel != null) {
                try {
                    inChannel.close();
                } catch (IOException e) {
                    Log.d(LOGTAG, "SaveImage. Error closing input file channel: ", e);
                    // does not harm, do nothing
                }
            }
            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (IOException e) {
                    Log.d(LOGTAG, "SaveImage. Error closing output file channel: ", e);
                    // does not harm, do nothing
                }
            }
        }

        return newFile;
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
