package com.example.eurka.comp90018;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseAdapter {
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_EMERGENCY_CONTACT = "emergencycontact";
    public static final String KEY_ROWID = "_id";

    private DatabaseHelper mDbHelper;
    public static SQLiteDatabase mDb;



    private static final String DATABASE_CREATE = "create table user (_id integer primary key autoincrement, "
            + "username text not null, password text not null, emergencycontact text not null);";

    private static final String DATABASE_NAME = "database";
    private static final String DATABASE_TABLE = "user";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    public static class DatabaseHelper extends SQLiteOpenHelper {


        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }
        public void insert(ContentValues values)
        {
            SQLiteDatabase db=getWritableDatabase();
            db.insert(DATABASE_TABLE, null, values);
            db.close();
        }
        public void close()
        {
            if(mDb!=null)
                mDb.close();
        }
        public Cursor query()
        {
            SQLiteDatabase db=getWritableDatabase();
            Cursor c=db.query(DATABASE_TABLE,null , null, null, null, null, null);
            return c;
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS user");
            onCreate(db);
        }
    }

    public DatabaseAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public DatabaseAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void closeclose() {
        mDbHelper.close();
    }

    public long createUser(String username, String password, String contact) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_USERNAME, username);
        initialValues.put(KEY_PASSWORD, password);
        initialValues.put(KEY_EMERGENCY_CONTACT, contact);
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }



    public Cursor getAllNotes() {

        return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_USERNAME,
                KEY_PASSWORD,KEY_EMERGENCY_CONTACT }, null, null, null, null, null);
    }

    public Cursor getDiary(String username) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[] { KEY_ROWID, KEY_USERNAME,
                                KEY_PASSWORD,KEY_EMERGENCY_CONTACT }, KEY_USERNAME + "='" + username+"'", null, null,
                        null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }






}
