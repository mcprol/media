package com.getcapacitor.community.media.callexecutor;

import android.Manifest;
import android.os.Environment;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;

public class SaveVideoCallExecutor extends SaveMediaCallExecutor {
    public SaveVideoCallExecutor(Plugin plugin, int requestCode) {
        super(plugin, requestCode, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public JSObject _execute(PluginCall call) throws Exception {
        return saveMedia(call, Environment.DIRECTORY_MOVIES);
    }
}