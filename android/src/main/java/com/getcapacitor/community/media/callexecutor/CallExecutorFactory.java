package com.getcapacitor.community.media.callexecutor;

import android.util.Log;

import com.getcapacitor.Plugin;
import com.getcapacitor.community.media.MediaPlugin;

public class CallExecutorFactory {

    private static final String LOGTAG = "MediaPlugin/CallExec";

    public static CallExecutor getInstance(Plugin plugin, int requestCode) {
        switch (requestCode) {
            case MediaPlugin.REQUEST_CODE_GET_ALBUMS:
                return new GetAlbumsCallExecutor(plugin, requestCode);

            case MediaPlugin.REQUEST_CODE_CREATE_ALBUM:
                return new CreateAlbumCallExecutor(plugin, requestCode);

            case MediaPlugin.REQUEST_CODE_SAVE_IMAGE:
                return new SaveImageCallExecutor(plugin, requestCode);

            case MediaPlugin.REQUEST_CODE_SAVE_VIDEO:
                return new SaveVideoCallExecutor(plugin, requestCode);

            case MediaPlugin.REQUEST_CODE_STORAGE_PERMISSION:
                return new StoragePermissionCallExecutor(plugin, requestCode);

            default:
                Log.e(LOGTAG, "Unknown CallExecutor for requestCode: " + requestCode);
                return null;
        }
    }
}