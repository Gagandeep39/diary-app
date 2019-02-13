package com.gagandeep.databasesync;

public class DbContract {
    public static final int SYNC_STATUS_OK = 0;
    public static final int SYNC_STATUS_FAILED = 1;
    public static final int DELETED = 1;
    public static final int NOT_DELETED = 0;
    static String webhostURL = "http://pokemonpokemon.000webhostapp.com/server_diary.php";
    public static final String SERVER_URL = webhostURL;

    public static final String DATABASE_NAME = "contactdb";
    public static final String TABLE_NAME = "diary";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String SYNC_STATUS = "syncstatus";
    public static final String UPDATED_ON = "updated_on";
    public static final String DELETE_STATUS = "deletestatus";

}
