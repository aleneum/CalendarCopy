package com.github.aleneum.calendarcopy;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RelationDatabaseHelper extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "relations";
    public static final String _ID = "id";
    public static final String SOURCE_EVENT = "SourceEvent";
    public static final String SOURCE_CALENDAR = "SourceCalendar";
    public static final String TARGET_EVENT = "TargetEvent";
    public static final String TARGET_CALENDAR = "TargetCalendar";

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "calendarcopy";
    private static final String TABLE_CREATE = " CREATE TABLE " + TABLE_NAME + " ( " +
            _ID + " INT PRIMARY KEY, "+
            SOURCE_EVENT + " Long NOT NULL, " +
            SOURCE_CALENDAR + " Long NOT NULL, " +
            TARGET_EVENT + " Long NOT NULL, " +
            TARGET_CALENDAR + " Long NOT NULL);";

    private static final String TABLE_DROP =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static final String DEBUG_TAG = "ccopy.DatabaseHelper";

    RelationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(DEBUG_TAG, TABLE_CREATE);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(TABLE_DROP);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}