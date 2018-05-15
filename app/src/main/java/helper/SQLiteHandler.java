package helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "android_api";

    // Login table name
    public static final String TABLE_USER = "users";
    public static final String TABLE_PLACES= "places";
    public static final String TABLE_ORDER= "orders";

    // Login Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_IDD = "_id";
    public static final String KEY_ID_ORD="id_ord";
    public static final String KEY_NAME = "name";
    public static final String KEY_SURNAME= "surname";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_ADDRESS ="address";
    public static final String KEY_UID = "uid";
    public static final String KEY_CREATED_AT = "created_at";
    public static final String KEY_COUNT_PLACE = "count_place";
    public static final String KEY_ORDER_TIME_START="time_s";
    public static final String KEY_ORDER_DATE_START="date_s";


    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE_USER = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE," + KEY_UID + " TEXT,"
                + KEY_CREATED_AT + " TEXT" + ")";
        String CREATE_LOGIN_TABLE_PLACES = "CREATE TABLE " + TABLE_PLACES + "("
                + KEY_IDD + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_NAME + " TEXT,"
                + KEY_ADDRESS + " TEXT," + KEY_UID + " TEXT,"
                + KEY_CREATED_AT + " TEXT, " + KEY_COUNT_PLACE + " TEXT " + ")";
        String CREATE_LOGIN_TABLE_ORDER = "CREATE TABLE " + TABLE_ORDER + "("
                + KEY_ID_ORD + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_NAME + " TEXT,"
                + KEY_SURNAME + " TEXT," + KEY_UID + " TEXT,"
                + KEY_CREATED_AT + " TEXT," + KEY_ORDER_DATE_START + " TEXT, " + KEY_ORDER_TIME_START + " TEXT " + ")";
        db.execSQL(CREATE_LOGIN_TABLE_USER);
        db.execSQL(CREATE_LOGIN_TABLE_PLACES);
        db.execSQL(CREATE_LOGIN_TABLE_ORDER);
        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER);
        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     * */
    public void addUser(String name, String email, String uid, String created_at) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Name
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_UID, uid); // Uid
        values.put(KEY_CREATED_AT, created_at); // Created At

        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New user inserted into sqlite: " + id);
    }
    public void addOrder(String name, String surname, String uid, String created_at, String date_s, String time_s) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Name
        values.put(KEY_SURNAME, surname); // Surname
        values.put(KEY_UID, uid); // Uid
        values.put(KEY_CREATED_AT, created_at); // Created At
        values.put(KEY_ORDER_DATE_START, date_s);
        values.put(KEY_ORDER_TIME_START, time_s);
        // Inserting Row
        long id = db.insert(TABLE_ORDER, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New order inserted into sqlite: " + id);
    }

    public void addPlace(String name, String address, String uid, String created_at, String count_place) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Name
        values.put(KEY_ADDRESS, address); // Address
        values.put(KEY_UID, uid); // Uid
        values.put(KEY_CREATED_AT, created_at); // Created At
        values.put(KEY_COUNT_PLACE, count_place);
        // Inserting Row
        long id = db.insert(TABLE_PLACES, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New place inserted into sqlite: " + id);
    }

    /**
     * Getting user data from database
     * */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("name", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("uid", cursor.getString(3));
            user.put("created_at", cursor.getString(4));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }

    public HashMap<String, String> getOrderDetails() {
        HashMap<String, String> order = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_ORDER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            order.put("name", cursor.getString(1));
            order.put("email", cursor.getString(2));
            order.put("uid", cursor.getString(3));
            order.put("created_at", cursor.getString(4));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching order from Sqlite: " + order.toString());

        return order;
    }
    public Cursor getPlaceDetails() {
        String selectQuery = "SELECT  * FROM " + TABLE_PLACES;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
                return cursor;
    }

    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }

    public void deletePlaces() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_PLACES, null, null);
        db.close();

        Log.d(TAG, "Deleted all places info from sqlite");
    }
    public void deleteOrders() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_ORDER, null, null);
        db.close();

        Log.d(TAG, "Deleted all order info from sqlite");
    }

}