package com.getcapacitor.community.media.callexecutor;

import android.util.Log;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;

public abstract class CallExecutor {
    protected static final String LOG_TAG = "MediaPlugin/CallExec";

    protected Plugin plugin;
    protected int requestCode;
    protected String permission;

    CallExecutor(Plugin plugin, int requestCode, String permission) {
        this.plugin = plugin;
        this.requestCode = requestCode;
        this.permission = permission;
    }

    public void run(PluginCall call) {
        try {
            if (plugin.hasPermission(permission)) {
                Log.d(LOG_TAG, permission + " already granted");
                execute(call);
            } else {
                Log.d(LOG_TAG, "requesting for " + permission);
                plugin.saveCall(call);
                plugin.pluginRequestPermission(permission, requestCode);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "[EXCEPTION]: " + e.getMessage(), e);
            call.error(e.getMessage(), e);
        }
    }

    public String getPermission() {
        return permission;
    }

    public int getRequestCode() {
        return requestCode;
    }

    // internal execution method for each PluginMethod.
    public abstract void execute(PluginCall call);
}
