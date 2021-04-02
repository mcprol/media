package com.getcapacitor.community.media;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;

import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.community.media.callexecutor.CallExecutor;
import com.getcapacitor.community.media.callexecutor.CallExecutorFactory;
import com.getcapacitor.community.media.callexecutor.StoragePermissionCallExecutor;

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
        MediaPlugin.REQUEST_CODE_STORAGE_PERMISSION
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
        CallExecutor executor = CallExecutorFactory.getInstance(this, REQUEST_CODE_GET_ALBUMS);
        executor.run(call);
    }

    @PluginMethod()
    public void createAlbum(PluginCall call) {
        CallExecutor executor = CallExecutorFactory.getInstance(this, REQUEST_CODE_CREATE_ALBUM);
        executor.run(call);
    }

    @PluginMethod()
    public void savePhoto(PluginCall call) {
        CallExecutor executor = CallExecutorFactory.getInstance(this, REQUEST_CODE_SAVE_IMAGE);
        executor.run(call);
    }

    @PluginMethod()
    public void saveVideo(PluginCall call) {
        CallExecutor executor = CallExecutorFactory.getInstance(this, REQUEST_CODE_SAVE_VIDEO);
        executor.run(call);
    }

    @PluginMethod()
    public void saveGif(PluginCall call) {
        CallExecutor executor = CallExecutorFactory.getInstance(this, REQUEST_CODE_SAVE_IMAGE);
        executor.run(call);
    }

    @PluginMethod()
    public void hasStoragePermission(PluginCall call) {
        CallExecutor executor = CallExecutorFactory.getInstance(this, REQUEST_CODE_STORAGE_PERMISSION);
        StoragePermissionCallExecutor storagePermissionCallExecutor = (StoragePermissionCallExecutor)executor;

        if (storagePermissionCallExecutor.hasStoragePermission()) {
            call.success();
        } else {
            call.error("Storage permission was denied");
        }
    }

    @PluginMethod()
    public void requestStoragePermission(PluginCall call) {
        CallExecutor executor = CallExecutorFactory.getInstance(this, REQUEST_CODE_STORAGE_PERMISSION);
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
        CallExecutor executor = CallExecutorFactory.getInstance(this, requestCode);
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
}
