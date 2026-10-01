package com.hf.tr760.fingerprint;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SyncDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sync_enrollments.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_ENROLLMENTS = "enrollments";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_UIN = "uin";
    public static final String COLUMN_JSON = "json_payload";
    public static final String COLUMN_NOM = "nom";
    public static final String COLUMN_PRENOM = "prenom";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_SYNCED = "is_synchronized";

    public SyncDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_ENROLLMENTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_UIN + " TEXT,"
                + COLUMN_JSON + " TEXT,"
                + COLUMN_NOM + " TEXT,"
                + COLUMN_PRENOM + " TEXT,"
                + COLUMN_TIMESTAMP + " INTEGER,"
                + COLUMN_SYNCED + " INTEGER" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENROLLMENTS);
        onCreate(db);
    }

    public long insertEnrollment(PendingEnrollment enrollment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_UIN, enrollment.getUin());
        values.put(COLUMN_JSON, enrollment.getJsonPayload());
        values.put(COLUMN_NOM, enrollment.getNom());
        values.put(COLUMN_PRENOM, enrollment.getPrenom());
        values.put(COLUMN_TIMESTAMP, enrollment.getTimestamp());
        values.put(COLUMN_SYNCED, enrollment.isSynchronized() ? 1 : 0);

        long id = db.insert(TABLE_ENROLLMENTS, null, values);
        db.close();
        return id;
    }

    public void updateSyncStatus(long id, boolean isSynced) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SYNCED, isSynced ? 1 : 0);
        db.update(TABLE_ENROLLMENTS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<PendingEnrollment> getAllEnrollments() {
        List<PendingEnrollment> list = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ENROLLMENTS + " ORDER BY " + COLUMN_TIMESTAMP + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                PendingEnrollment e = new PendingEnrollment();
                e.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                e.setUin(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UIN)));
                e.setJsonPayload(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_JSON)));
                e.setNom(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOM)));
                e.setPrenom(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRENOM)));
                e.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));
                e.setSynchronized(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SYNCED)) == 1);
                list.add(e);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
}
