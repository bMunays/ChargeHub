package org.edupage.brighperc.a48175.chargehub;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "chargehub.db";
    private static final int DATABASE_VERSION = 1;

    // Table and Columns
    private static final String TABLE_SESSIONS = "sessions";
    private static final String COL_SESSION_ID = "session_id";
    private static final String COL_SLOT_NUMBER = "slot_number";
    private static final String COL_PRIMARY_PHONE = "primary_phone";
    private static final String COL_ALT_PHONE = "alt_phone";
    private static final String COL_NATIONAL_ID = "national_id";
    private static final String COL_PHOTO_URI = "photo_uri";
    private static final String COL_CHECK_IN_TIME = "check_in_time";
    private static final String COL_CHECK_OUT_TIME = "check_out_time";
    private static final String COL_STATUS = "status";
    private static final String COL_FEE = "fee_collected";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_SESSIONS + " (" +
                COL_SESSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SLOT_NUMBER + " INTEGER NOT NULL, " +
                COL_PRIMARY_PHONE + " TEXT NOT NULL, " +
                COL_ALT_PHONE + " TEXT, " +
                COL_NATIONAL_ID + " TEXT, " +
                COL_PHOTO_URI + " TEXT, " +
                COL_CHECK_IN_TIME + " DATETIME DEFAULT (datetime('now', 'localtime')), " +
                COL_CHECK_OUT_TIME + " DATETIME, " +
                COL_STATUS + " TEXT DEFAULT 'CHARGING', " +
                COL_FEE + " REAL DEFAULT 0.0)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSIONS);
        onCreate(db);
    }

    // --- CHECK IN ---
    public long checkInPhone(int slot, String phone, String altPhone, String nationalId, String photoUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SLOT_NUMBER, slot);
        values.put(COL_PRIMARY_PHONE, phone);
        values.put(COL_ALT_PHONE, altPhone);
        values.put(COL_NATIONAL_ID, nationalId);
        values.put(COL_PHOTO_URI, photoUri);
        // check_in_time and status are handled by DEFAULT in the schema

        long result = db.insert(TABLE_SESSIONS, null, values);
        db.close();
        return result; // Returns the new session_id, or -1 if failed
    }

    // --- CHECK OUT ---
    public boolean checkOutPhone(int slot, double feeCalculated) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, "COMPLETED");
        values.put(COL_FEE, feeCalculated);
        // Uses SQLite's datetime function to stamp the exact local checkout time

        String selection = COL_SLOT_NUMBER + "=? AND " + COL_STATUS + "=?";
        String[] selectionArgs = { String.valueOf(slot), "CHARGING" };

        // We first need to execute a raw SQL to use the datetime function for check_out_time,
        // OR we can pass it as a raw string.
        // For simplicity and safety with ContentValues, we do an update call then a raw SQL for the timestamp:
        int rowsAffected = db.update(TABLE_SESSIONS, values, selection, selectionArgs);

        if(rowsAffected > 0) {
            String timeQuery = "UPDATE " + TABLE_SESSIONS + " SET " + COL_CHECK_OUT_TIME + " = datetime('now', 'localtime') WHERE " + selection;
            db.execSQL(timeQuery, selectionArgs);
        }

        db.close();
        return rowsAffected > 0;
    }

    // --- DASHBOARD READ ---
    // Gets all slots currently charging to populate the UI Grid
    public Cursor getActiveSessions() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_SESSIONS, null, COL_STATUS + "='CHARGING'", null, null, null, COL_SLOT_NUMBER + " ASC");
    }
}