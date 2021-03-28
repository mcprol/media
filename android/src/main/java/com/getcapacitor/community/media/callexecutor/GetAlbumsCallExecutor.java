package com.getcapacitor.community.media.callexecutor;

import android.Manifest;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;

public class GetAlbumsCallExecutor extends CallExecutor {
    private static final String LOGTAG = "MediaPlugin/CallExec";

    public GetAlbumsCallExecutor(Plugin plugin, int requestCode) {
        super(plugin, requestCode, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public JSObject _execute(PluginCall call) throws Exception {
        return getAlbums(call);
    }

    private JSObject getAlbums(PluginCall call) {
        Log.d(LOGTAG, "___GET ALBUMS");

        JSObject response = new JSObject();
        JSArray albums = new JSArray();
        StringBuffer list = new StringBuffer();

        String[] projection = new String[]{"DISTINCT " + MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME};
        Cursor cur = plugin.getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);

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

        return response;
    }
}