package com.getcapacitor.community.media.callexecutor;

import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;

public abstract class CallExecutor {
    private static final String LOGTAG = "MediaPlugin/CallExec";

    protected Plugin plugin;
    protected int requestCode;
    protected String permission;

    public CallExecutor(Plugin plugin, int requestCode, String permission) {
        this.plugin = plugin;
        this.requestCode = requestCode;
        this.permission = permission;
    }

    public void run(PluginCall call) {
        try {
            if (plugin.hasPermission(permission)) {
                Log.d(LOGTAG, permission + " already granted");
                execute(call);
            } else {
                Log.d(LOGTAG, "requesting for " + permission);
                plugin.saveCall(call);
                plugin.pluginRequestPermission(permission, requestCode);
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "[EXCEPTION]: " + e.getMessage(), e);
            call.reject(e.getMessage(), e);
        }
    }

    public void execute(PluginCall call) {
        try {
            JSObject response = _execute(call);
            if (response == null) {
                call.resolve();
            } else {
                call.resolve(response);
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "[EXCEPTION]: " + e.getMessage(), e);
            call.reject(e.getMessage(), e);
        }
    }

    public String getPermission() {
        return permission;
    }

    public int getRequestCode() {
        return requestCode;
    }

    // internal execution method for each PluginMethod.
    // Returns:
    //   a valid JSON object on success with data to return as 'resolve' to the calling promise,
    //   NULL on success but no data to return
    //   throws an exception on error. It will returned as 'reject' to the calling promise
    public abstract JSObject _execute(PluginCall call) throws Exception;
}
