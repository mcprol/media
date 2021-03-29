package com.getcapacitor.community.media.callexecutor;

import android.Manifest;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;

public class StoragePermissionCallExecutor extends SyncCallExecutor {
    public StoragePermissionCallExecutor(Plugin plugin, int requestCode) {
        super(plugin, requestCode, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public JSObject syncExecute(PluginCall call) throws Exception {
        if (!hasStoragePermission()) {
            throw new Exception ("Storage permission was denied");
        }

        return null;
    }

    public boolean hasStoragePermission() {
        return plugin.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
}