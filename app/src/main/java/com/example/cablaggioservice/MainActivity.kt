package com.example.cablaggioservice

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import androidx.annotation.ArrayRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.example.cablaggioservice.databinding.ActivityMainBinding
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val t = supportFragmentManager.beginTransaction()
        t.replace(R.id.container_main, FragSezioni())
        t.commit()

        //creo gli sharedPreferences di default
        val sharedPreferences = getSharedPreferences("preset", Context.MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)

        if (isFirstRun) {
            val dbHelper = MyDatabaseHelper(applicationContext)
            val db = dbHelper.writableDatabase

            //do alla funzione i dati che gli servono
            createAndSetJsonArray(db, R.array.batteria1String, "batteria1String", R.string.batteria_1, R.id.group_batteria)
            createAndSetJsonArray(
                db,
                R.array.batteria2String,
                "batteria2String",
                R.string.batteria_2,
                R.id.group_batteria
            )
            createAndSetJsonArray(
                db,
                R.array.batteria3String,
                "batteria3String",
                R.string.batteria_3,
                R.id.group_batteria
            )
            createAndSetJsonArray(
                db,
                R.array.batteria4String,
                "batteria4String",
                R.string.batteria_4,
                R.id.group_batteria
            )

            createAndSetJsonArray(
                db,
                R.array.tastiera1String,
                "tastiera1String",
                R.string.tastiera_1,
                R.id.group_tastiera
            )
            createAndSetJsonArray(
                db,
                R.array.tastiera2String,
                "tastiera2String",
                R.string.tastiera_2,
                R.id.group_tastiera
            )
            createAndSetJsonArray(
                db,
                R.array.tastiera3String,
                "tastiera3String",
                R.string.tastiera_3,
                R.id.group_tastiera
            )
            createAndSetJsonArray(
                db,
                R.array.tastiera4String,
                "tastiera4String",
                R.string.tastiera_4,
                R.id.group_tastiera
            )
            createAndSetJsonArray(
                db,
                R.array.tastiera5String,
                "tastiera5String",
                R.string.tastiera_5,
                R.id.group_tastiera
            )
            createAndSetJsonArray(
                db,
                R.array.tastiera6String,
                "tastiera6String",
                R.string.tastiera_6,
                R.id.group_tastiera
            )
            createAndSetJsonArray(
                db,
                R.array.tastiera7String,
                "tastiera7String",
                R.string.tastiera_7,
                R.id.group_tastiera
            )
            createAndSetJsonArray(
                db,
                R.array.tastiera8String,
                "tastiera8String",
                R.string.tastiera_8,
                R.id.group_tastiera
            )
            createAndSetJsonArray(
                db,
                R.array.tastiera9String,
                "tastiera9String",
                R.string.tastiera_9,
                R.id.group_tastiera
            )
            createAndSetJsonArray(
                db,
                R.array.tastiera10String,
                "tastiera10String",
                R.string.tastiera_10,
                R.id.group_tastiera
            )
            createAndSetJsonArray(
                db,
                R.array.percussioni1String,
                "percussioni1String",
                R.string.percussioni_1,
                R.id.group_percussioni
            )
            createAndSetJsonArray(
                db,
                R.array.chitarra1String,
                "chitarra1String",
                R.string.chitarra_1,
                R.id.group_check_chitarra
            )
            createAndSetJsonArray(
                db,
                R.array.voci1String,
                "voci1String",
                R.string.voci_1,
                R.id.group_voci
            )
            createAndSetJsonArray(
                db,
                R.array.cori1String,
                "cori1String",
                R.string.cori_1,
                R.id.group_cori
            )

            Log.i("msg", "applico dati iniziali predefiniti")

            db.close()

            // Imposta isFirstRun a false per indicare che le preferenze sono state impostate
            sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
        }
    }

    private fun createAndSetJsonArray(
        db: SQLiteDatabase,
        @ArrayRes redId: Int,
        key: String,
        @StringRes nomePreset: Int,
        @IdRes nomeGroup: Int
    ) {
        val pattern = Regex(".*?\\d+String")

        if (!pattern.matches(key)) {
           throw IllegalArgumentException("$key non segue il pattern di chiave predefinito")
        }

        val stringArray = resources.getStringArray(redId).toList()

        //converto stringArray in jsonString per salvare l'intero array
        val jsonArray = JSONArray(stringArray)
        val jsonString = jsonArray.toString()

        val values = ContentValues().apply {
            put(MyDatabaseHelper.FeedReaderContract.FeedEntry.ID, key)
            put(MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_PRESET, resources.getString(nomePreset))
            put(MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_ARRAY, jsonString)
            put(MyDatabaseHelper.FeedReaderContract.FeedEntry.GROUPS, nomeGroup)
        }

        //newRowId conterrà l'ID della nuova riga inserita, o -1 se si è verificato un errore durante l'inserimento
        val newRowId = db.insert(MyDatabaseHelper.FeedReaderContract.FeedEntry.TABLE_NAME, null, values)

        if(newRowId < 0){
            Log.i("msg", "ERRORE INSERIMENTO VALORI QUERY")
        }else{
            Log.i("msg", "QUERY INSERITA CORRETTAMENTE")
        }
    }


}