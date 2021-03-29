package com.getcapacitor.community.media.callexecutor;

import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;

public abstract class SyncCallExecutor extends CallExecutor {

    public SyncCallExecutor(Plugin plugin, int requestCode, String permission) {
        super(plugin, requestCode, permission);
    }

    public void execute(PluginCall call) {
        try {
            JSObject response = syncExecute(call);
            if (response == null) {
                call.resolve();
            } else {
                call.resolve(response);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "[EXCEPTION]: " + e.getMessage(), e);
            call.reject(e.getMessage(), e);
        }
    }

    // internal execution method for each PluginMethod.
    // Returns:
    //   a valid JSON object on success with data to return as 'resolve' to the calling promise,
    //   NULL on success but no data to return
    //   throws an exception on error. It will returned as 'reject' to the calling promise
    public abstract JSObject syncExecute(PluginCall call) throws Exception;
}
