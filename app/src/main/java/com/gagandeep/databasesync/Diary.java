package com.gagandeep.databasesync;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Diary {
    String title;
    String description;
    int syncstatus;
    String updatedOn;
    long _id;
    int deleteStatus;

    public Diary() {
    }

    public Diary(long _id, String title, String description, int syncstatus, String updatedOn, int deleteStatus) {
        this._id = _id;
        this.title = title;
        this.description = description;
        this.syncstatus = syncstatus;
        this.updatedOn = updatedOn;
        this.deleteStatus = deleteStatus;
    }

    public int getDeleteStatus() {
        return deleteStatus;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getSyncstatus() {
        return syncstatus;
    }

    public String getUpdatedOn() {
        return updatedOn;
    }

    public long get_id() {
        return _id;
    }

    public JSONObject getJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("_id", _id);
            obj.put("title", title);
            obj.put("description", description);
            obj.put("syncStatus", syncstatus);
            obj.put("updatedOn", updatedOn);
            obj.put("deleteStatus", deleteStatus);
        } catch (JSONException e) {
            Log.e("", "getJSONObject: " + e );
        }
        return obj;
    }
}
