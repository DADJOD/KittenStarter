package com.example.kittenstarter

import android.database.sqlite.SQLiteDatabase


object PhotosTable {
    const val TABLE_PHOTOS = "photos"
    private const val COLUMN_ID = "_id"
    const val COLUMN_URL = "url"
    private const val PHOTOS_CREATE = ("create table "
            + TABLE_PHOTOS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_URL
            + " text not null);")

    fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        sqLiteDatabase.execSQL(PHOTOS_CREATE)
    }

    fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS $TABLE_PHOTOS")
        onCreate(sqLiteDatabase)
    }
}