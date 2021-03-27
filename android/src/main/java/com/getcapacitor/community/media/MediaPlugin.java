package com.getcapacitor.community.media;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import java.net.URL;

@NativePlugin(
        permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET
        },
        requestCodes = {
                MediaPlugin.REQUEST_CODE_GET_ALBUMS,
                MediaPlugin.REQUEST_CODE_CREATE_ALBUM,
                MediaPlugin.REQUEST_CODE_SAVE_IMAGE,
                MediaPlugin.REQUEST_CODE_SAVE_VIDEO,
                MediaPlugin.REQUEST_CODE_STORAGE_PERMISSION,
        }
)
public class MediaPlugin extends Plugin {

    private static final String LOGTAG = "MediaPlugin";

    private static final int REQUEST_CODE_BASE = 2020;
    public static final int REQUEST_CODE_GET_ALBUMS = REQUEST_CODE_BASE + 1;
    public static final int REQUEST_CODE_CREATE_ALBUM = REQUEST_CODE_BASE + 2;
    public static final int REQUEST_CODE_SAVE_IMAGE = REQUEST_CODE_BASE + 3;
    public static final int REQUEST_CODE_SAVE_VIDEO = REQUEST_CODE_BASE + 4;
    public static final int REQUEST_CODE_STORAGE_PERMISSION = REQUEST_CODE_BASE + 5;

    // @todo
    @PluginMethod()
    public void getMedias(PluginCall call) {
        call.unimplemented();
    }

    // @todo
    @PluginMethod()
    public void getPhotos(PluginCall call) {
        call.unimplemented();
    }

    @PluginMethod()
    public void getAlbums(PluginCall call) {
        CallExecutor executor = getCallExecutor(REQUEST_CODE_GET_ALBUMS);
        executor.run(call);
    }

    @PluginMethod()
    public void createAlbum(PluginCall call) {
        CallExecutor executor = getCallExecutor(REQUEST_CODE_CREATE_ALBUM);
        executor.run(call);
    }

    @PluginMethod()
    public void savePhoto(PluginCall call) {
        CallExecutor executor = getCallExecutor(REQUEST_CODE_SAVE_IMAGE);
        executor.run(call);
    }

    @PluginMethod()
    public void saveVideo(PluginCall call) {
        CallExecutor executor = getCallExecutor(REQUEST_CODE_SAVE_VIDEO);
        executor.run(call);
    }

    @PluginMethod()
    public void saveGif(PluginCall call) {
        CallExecutor executor = getCallExecutor(REQUEST_CODE_SAVE_IMAGE);
        executor.run(call);
    }

    @PluginMethod()
    public void hasStoragePermission(PluginCall call) {
        if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            call.success();
        } else {
            call.error("StoragePermission was denied");
        }
    }

    @PluginMethod()
    public void requestStoragePermission(PluginCall call) {
        CallExecutor executor = getCallExecutor(REQUEST_CODE_STORAGE_PERMISSION);
        executor.run(call);
    }

    @Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(LOGTAG, "handleRequestPermissionsResult: " + requestCode);

        PluginCall call = getSavedCall();
        if (call == null) {
            Log.d(LOGTAG,"No stored PluginCall for permissions request result");
            return;
        }

        for(int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                call.error("Permission denied for requestCode: " + requestCode);
                return;
            }
        }
        boolean permissionGranted = true;

        CallExecutor executor = getCallExecutor(requestCode);
        if (executor != null) {
            if (permissionGranted) {
                executor.execute(call);
            } else {
                call.error("Permission denied for requestCode: " + requestCode);
            }
        } else {
            call.error("Unknown CallExecutor for requestCode: " + requestCode);
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

    private void _createAlbum(PluginCall call) {
        String folderName = call.getString("name");
        Log.d(LOGTAG, "___CREATE ALBUM: " + String.valueOf(folderName));

        String folder;

        if (Build.VERSION.SDK_INT >= 29) {
            folder = getContext().getExternalMediaDirs()[0].getAbsolutePath()+"/"+folderName;
        } else {
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

    private void _saveMedia(PluginCall call, String destination) {
        Log.d(LOGTAG, "___SAVE MEDIA TO ALBUM: " + String.valueOf(destination));

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

    private CallExecutor getCallExecutor(int requestCode) {
        switch (requestCode) {
            case REQUEST_CODE_GET_ALBUMS:
                return new GetAlbumsCallExecutor(requestCode);
            case REQUEST_CODE_CREATE_ALBUM:
                return new CreateAlbumCallExecutor(requestCode);
            case REQUEST_CODE_SAVE_IMAGE:
                return new SaveImageCallExecutor(requestCode);
            case REQUEST_CODE_SAVE_VIDEO:
                return new SaveVideoCallExecutor(requestCode);
            case REQUEST_CODE_STORAGE_PERMISSION:
                return new StoragePermissionCallExecutor(requestCode);
            default:
                Log.e(LOGTAG,"Unknown CallExecutor for requestCode: " + requestCode);
                return null;
        }
    }

    private abstract class CallExecutor {
        protected int requestCode;
        protected String permission;

        public CallExecutor(int requestCode, String permission) {
            this.requestCode = requestCode;
            this.permission = permission;
        }

        public void run(PluginCall call) {
            try {
                if (hasPermission(permission)) {
                    Log.d(LOGTAG, permission + " already granted");
                    execute(call);
                } else {
                    Log.d(LOGTAG, "requesting for " + permission);
                    saveCall(call);
                    pluginRequestPermission(permission, requestCode);
                }
            } catch (Exception e) {
                Log.e(LOGTAG, "[EXCEPTION]: " + e.getMessage(), e);
                call.error(e.getMessage(), e);
            }
        }

        public void execute(PluginCall call) {
            try {
                _execute(call);
            } catch (Exception e) {
                Log.e(LOGTAG, "[EXCEPTION]: " + e.getMessage(), e);
                call.error(e.getMessage(), e);
            }
        }

        public String getPermission() {
            return permission;
        }

        public int getRequestCode() {
            return requestCode;
        }

        public abstract void _execute(PluginCall call);
    }

    private class GetAlbumsCallExecutor extends CallExecutor {
        public GetAlbumsCallExecutor(int requestCode) {
            super(requestCode, Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        public void _execute(PluginCall call) {
            _getAlbums(call);
        }
    }

    private class CreateAlbumCallExecutor extends CallExecutor {
        public CreateAlbumCallExecutor(int requestCode) {
            super(requestCode, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        public void _execute(PluginCall call) {
            _createAlbum(call);
        }
    }

    private class SaveImageCallExecutor extends CallExecutor {
        public SaveImageCallExecutor(int requestCode) {
            super(requestCode, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        public void _execute(PluginCall call) {
            _saveMedia(call, Environment.DIRECTORY_PICTURES);
        }
    }

    private class SaveVideoCallExecutor extends CallExecutor {
        public SaveVideoCallExecutor(int requestCode) {
            super(requestCode, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        public void _execute(PluginCall call) {
            _saveMedia(call, Environment.DIRECTORY_MOVIES);
        }
    }

    private class StoragePermissionCallExecutor extends CallExecutor {
        public StoragePermissionCallExecutor(int requestCode) {
            super(requestCode, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        public void _execute(PluginCall call) {
            hasStoragePermission(call);
        }
    }
}
