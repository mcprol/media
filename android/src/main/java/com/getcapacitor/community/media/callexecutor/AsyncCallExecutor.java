package com.getcapacitor.community.media.callexecutor;

import android.os.AsyncTask;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;

public abstract class AsyncCallExecutor extends CallExecutor {

    public AsyncCallExecutor(Plugin plugin, int requestCode, String permission) {
        super(plugin, requestCode, permission);
    }

    public void execute(PluginCall call) {
        try {
            AsyncTaskExecutor asyncTaskExecutor = new AsyncTaskExecutor(call);
            asyncTaskExecutor.execute();
        } catch (Exception e) {
            Log.e(LOG_TAG, "[EXCEPTION]: " + e.getMessage(), e);
            call.error(e.getMessage(), e);
        }
    }

    // internal execution method for each PluginMethod.
    // Returns:
    //   a valid JSON object on success with data to return as 'resolve' to the calling promise,
    //   NULL on success but no data to return
    //   throws an exception on error. It will returned as 'reject' to the calling promise
    public abstract JSObject asyncExecute(PluginCall call) throws Exception;


    // creates a new thread for async execution
    class AsyncTaskExecutor extends AsyncTask<String, Void, JSObject> {
        private Exception e = null;
        private PluginCall call;

        public AsyncTaskExecutor(PluginCall call) {
            this.call = call;
        }

        @Override
        protected JSObject doInBackground(String... params) {
            try {
                JSObject response = asyncExecute(call);
                return response;
            } catch (Exception e) {
                Log.e(LOG_TAG, "[EXCEPTION]: " + e.getMessage(), e);
                this.e = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSObject response) {
            // on error
            if (this.e != null) {
                call.error(e.getMessage(), e);
                return;
            }

            // on success
            if (response == null) {
                call.success();
            } else {
                call.success(response);
            }
        }
    }
}
