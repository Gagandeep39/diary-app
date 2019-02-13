package com.gagandeep.databasesync;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class DbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
//    public static final String CREATE_TABLE = "CREATE TABLE " + DbContract.TABLE_NAME + "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
//            DbContract.TITLE + " TEXT, " + DbContract.DESCRIPTION + " TEXT, " + DbContract.UPDATED_ON  +" DATETIME DEFAULT CURRENT_TIMESTAMP, " + DbContract.SYNC_STATUS + " INTEGER);";
    public static final String CREATE_TABLE = "CREATE TABLE " + DbContract.TABLE_NAME + "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DbContract.TITLE + " TEXT, " + DbContract.DESCRIPTION + " TEXT, " + DbContract.UPDATED_ON  +" DATETIME , "+ DbContract.DELETE_STATUS + " INTEGER DEFAULT " + DbContract.NOT_DELETED + ", " + DbContract.SYNC_STATUS + " INTEGER);";


    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + DbContract.TABLE_NAME;

    public DbHelper(Context context) {
        super(context, DbContract.DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

    public void saveToLocalDatabase(String name, String description, int syncStatus, String updatedOn, SQLiteDatabase database){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.TITLE, name);
        contentValues.put(DbContract.DESCRIPTION, description);
        contentValues.put(DbContract.SYNC_STATUS, syncStatus);
        contentValues.put(DbContract.UPDATED_ON, updatedOn);
        database.insert(DbContract.TABLE_NAME, null, contentValues);
    }


    public Cursor readFromLocalDatabase(SQLiteDatabase database){
        String[] projection = {"id", DbContract.TITLE, DbContract.DESCRIPTION, DbContract.UPDATED_ON, DbContract.SYNC_STATUS, DbContract.DELETE_STATUS};
        return database.query(DbContract.TABLE_NAME, projection, null, null, null, null, null);
    }

    public void updateLocalDatabase(String title, String description, int syncStatus, SQLiteDatabase database) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.DESCRIPTION, description);
        contentValues.put(DbContract.UPDATED_ON, getDateTime());
        contentValues.put(DbContract.SYNC_STATUS, syncStatus);
        String selection = DbContract.TITLE + " LIKE ?";
        String[] selection_args = {title};
        database.update(DbContract.TABLE_NAME, contentValues, selection, selection_args);
    }

    public void deleteFromLocalDatabase(SQLiteDatabase database){
        String whereClause = DbContract.DELETE_STATUS + " = 1";
        database.delete(DbContract.TABLE_NAME, whereClause, null);
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
