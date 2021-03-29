package com.getcapacitor.community.media.callexecutor;

import android.os.Environment;
import android.util.Log;

import com.getcapacitor.Plugin;
import com.getcapacitor.community.media.MediaPlugin;

public class CallExecutorFactory {

    private static final String LOG_TAG = "MediaPlugin/CallExec";

    public static CallExecutor getInstance(Plugin plugin, int requestCode) {
        switch (requestCode) {
            case MediaPlugin.REQUEST_CODE_GET_ALBUMS:
                return new GetAlbumsCallExecutor(plugin, requestCode);

            case MediaPlugin.REQUEST_CODE_CREATE_ALBUM:
                return new CreateAlbumCallExecutor(plugin, requestCode);

            case MediaPlugin.REQUEST_CODE_SAVE_IMAGE:
                return new SaveMediaCallExecutor(plugin, requestCode, Environment.DIRECTORY_PICTURES);

            case MediaPlugin.REQUEST_CODE_SAVE_VIDEO:
                return new SaveMediaCallExecutor(plugin, requestCode, Environment.DIRECTORY_MOVIES);

            case MediaPlugin.REQUEST_CODE_STORAGE_PERMISSION:
                return new StoragePermissionCallExecutor(plugin, requestCode);

            default:
                Log.e(LOG_TAG, "Unknown CallExecutor for requestCode: " + requestCode);
                return null;
        }
    }
}