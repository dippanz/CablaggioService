package com.example.cablaggioservice

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class MyDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // Crea le tabelle del database e definisci la loro struttura
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 3
        const val DATABASE_NAME = "dbCablaggio.db"

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${FeedReaderContract.FeedEntry.TABLE_NAME}"

        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${FeedReaderContract.FeedEntry.TABLE_NAME} (" +
                    "${FeedReaderContract.FeedEntry.ID} INTEGER PRIMARY KEY," +
                    "${FeedReaderContract.FeedEntry.COLUMN_NAME_PRESET} TEXT," +
                    "${FeedReaderContract.FeedEntry.COLUMN_NAME_ARRAY} TEXT," +
                    "${FeedReaderContract.FeedEntry.GROUPS} INTEGER)"


    }

    object FeedReaderContract {
        // Table contents are grouped together in an anonymous object.
        object FeedEntry : BaseColumns {
            const val TABLE_NAME = "preset"
            const val ID = "idPreset"
            const val COLUMN_NAME_PRESET = "nome"
            const val COLUMN_NAME_ARRAY = "jsonArray"
            const val GROUPS = "nomeGruppo"
        }
    }
}
