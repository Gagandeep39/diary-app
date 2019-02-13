package com.gagandeep.databasesync;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.gagandeep.databasesync.DbContract.SERVER_URL;
import static com.gagandeep.databasesync.DbContract.SYNC_STATUS_OK;

public class MainActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    Button buttonSave;
    EditText editTextName;

    ArrayList<Diary> arrayList = new ArrayList<>();
    String title;
    String description = "Description";
    RecyclerAdapter adapter;

    RequestQueue queue;

    DbHelper dbHelper;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSavedTimeStamp().equals(""))
            saveTimeStamp();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findViews();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);

        adapter = new RecyclerAdapter(arrayList);
        mRecyclerView.setAdapter(adapter);
        readFromLocalStorage();
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = editTextName.getText().toString();
                saveToLocalStorage(title, 1);
                editTextName.setText("");
                readFromLocalStorage();
            }
        });
    }

    private void findViews() {
        mRecyclerView = findViewById(R.id.listViewNames);
        buttonSave = findViewById(R.id.buttonSave);
        editTextName = findViewById(R.id.editTextName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sync) {
            syncData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        readFromLocalStorage();

    }

    private void syncData() {
        if (checkNetworkConnection(this)) {
            saveDataToServer();
        }
    }

    private void saveDataToServer() {

        String lastSync = getSavedTimeStamp();

        dbHelper = new DbHelper(this);
        database = dbHelper.getReadableDatabase();
        ArrayList<Diary> tempArrayList = new ArrayList<>();


        String[] projection = {"id", DbContract.TITLE, DbContract.DESCRIPTION, DbContract.UPDATED_ON, DbContract.SYNC_STATUS, DbContract.DELETE_STATUS};
        String selection = DbContract.UPDATED_ON + "> ?";
        String selectionArgs[] = {lastSync};
        JSONArray jsonArray = new JSONArray();
        Cursor queryCursor = database.query(DbContract.TABLE_NAME, projection, selection, selectionArgs, null, null, null);

        while (queryCursor.moveToNext()) {
            String currentTitle = queryCursor.getString(queryCursor.getColumnIndex(DbContract.TITLE));
            String currentDescription = queryCursor.getString(queryCursor.getColumnIndex(DbContract.DESCRIPTION));
            String currentUpdatedOn = queryCursor.getString(queryCursor.getColumnIndex(DbContract.UPDATED_ON));
            int syncStatus = queryCursor.getInt(queryCursor.getColumnIndex(DbContract.SYNC_STATUS));
            int id = queryCursor.getInt(queryCursor.getColumnIndex("id"));
            int deleteStatus = queryCursor.getInt(queryCursor.getColumnIndex(DbContract.DELETE_STATUS));
            tempArrayList.add(new Diary(id, currentTitle, currentDescription, syncStatus, currentUpdatedOn, deleteStatus));

        }
        try{for (int i = 0; i < tempArrayList.size(); i++)
            jsonArray.put(tempArrayList.get(i).getJSONObject().put("username", "Gagandeep"));}
        catch (JSONException e){
            e.printStackTrace();
        }
        Log.e("JSONArray", "saveDataToServer: "  + jsonArray);


        if (tempArrayList.size()>0){
            saveUsingVolley(jsonArray, tempArrayList);
        }else
            Toast.makeText(this, "No data to be updated", Toast.LENGTH_SHORT).show();
    }

    private void saveUsingVolley(JSONArray jsonArray, final ArrayList<Diary> tempArrayList) {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST,
                SERVER_URL, jsonArray,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                            processResponse(response, tempArrayList);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VolleyError", "onErrorResponse: " + error );

            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                        headers.put("Content-Type", "application/json; charset=utf-8");
                        headers.put("username", "Gagandeep");
//                headers.put("Accept", "application/json");
                return headers;
            }
        };
        queue.add(jsonArrayRequest);
    }

    private void processResponse(JSONArray response, ArrayList<Diary> tempArrayList) {

        database = dbHelper.getWritableDatabase();
        try{
            for(int i=0; i<tempArrayList.size(); i++){
                String tempTitle = tempArrayList.get(i).getTitle();
                Toast.makeText(this, ""+tempTitle, Toast.LENGTH_SHORT).show();

                if (response.getJSONObject(i).getString(tempTitle).equals("inserted") || response.getJSONObject(i).getString(tempTitle).equals("updated")){
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DbContract.SYNC_STATUS, SYNC_STATUS_OK);
                    String selection = DbContract.TITLE + " = " + "'" + tempTitle+"'";
                    database.update(DbContract.TABLE_NAME, contentValues, selection, null);

                    }

            }
        }
        catch (JSONException e){
            e.printStackTrace();
        }


        dbHelper.deleteFromLocalDatabase(database);
        saveTimeStamp();
        readFromLocalStorage();

    }


    private void saveToLocalStorage(String title, int syncStatus) {

        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        dbHelper.saveToLocalDatabase(title, description, syncStatus, getDateTime(),  database);
        dbHelper.close();
    }


    public boolean checkNetworkConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());

    }


    private void readFromLocalStorage() {

        arrayList.clear();
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        Cursor cursor = dbHelper.readFromLocalDatabase(database);
        while (cursor.moveToNext()) {
            String currentTitle = cursor.getString(cursor.getColumnIndex(DbContract.TITLE));
            String currentDescription = cursor.getString(cursor.getColumnIndex(DbContract.DESCRIPTION));
            String currentUpdatedOn = cursor.getString(cursor.getColumnIndex(DbContract.UPDATED_ON));
            int syncStatus = cursor.getInt(cursor.getColumnIndex(DbContract.SYNC_STATUS));
            long id = cursor.getLong(cursor.getColumnIndex("id"));
            int deleteStatus = cursor.getInt(cursor.getColumnIndex(DbContract.DELETE_STATUS));
            arrayList.add(new Diary(id, currentTitle, currentDescription, syncStatus, currentUpdatedOn, deleteStatus));

        }

        adapter.notifyDataSetChanged();
        cursor.close();
        dbHelper.close();
    }


    void saveTimeStamp() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("timestamp", getDateTime());
        editor.commit();
    }

    String getSavedTimeStamp() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String timeStamp = sharedPref.getString("timestamp", "");
        return timeStamp;
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = dateFormat.format(Calendar.getInstance().getTime());
        return date;
    }
}
