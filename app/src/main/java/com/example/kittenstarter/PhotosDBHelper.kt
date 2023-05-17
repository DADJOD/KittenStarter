package com.example.kittenstarter

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class PhotosDBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_FILE, null, DATABASE_VERSION) {
    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        PhotosTable.onCreate(sqLiteDatabase)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        PhotosTable.onUpgrade(sqLiteDatabase, i, i1)
    }

    companion object {
        private const val DATABASE_FILE = "photos.db"
        private const val DATABASE_VERSION = 1
    }
}