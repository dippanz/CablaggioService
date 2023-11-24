package com.example.cablaggioservice

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.ArrayRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.cablaggioservice.databinding.ActivityMainBinding
import org.json.JSONArray


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.title = ""
        setSupportActionBar(toolbar)

        val t = supportFragmentManager.beginTransaction()
        t.addToBackStack("principale")
        t.replace(R.id.container_main, FragSezioni())
        t.commit()

        //creo gli sharedPreferences di default
        val sharedPreferences = getSharedPreferences("preset", Context.MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)

        if (isFirstRun) {
            val dbHelper = MyDatabaseHelper(applicationContext)
            val db = dbHelper.writableDatabase

            // Batteria
            createAndSetJsonArray(db, R.array.batteria1String, generatePositiveUniqueId("batteria1"), R.string.batteria_1, R.id.group_batteria)
            createAndSetJsonArray(db, R.array.batteria2String, generatePositiveUniqueId("batteria2"), R.string.batteria_2, R.id.group_batteria)
            createAndSetJsonArray(db, R.array.batteria3String, generatePositiveUniqueId("batteria3"), R.string.batteria_3, R.id.group_batteria)
            createAndSetJsonArray(db, R.array.batteria4String, generatePositiveUniqueId("batteria4"), R.string.batteria_4, R.id.group_batteria)

            // Tastiera
            createAndSetJsonArray(db, R.array.tastiera1String, generatePositiveUniqueId("tastiera1"), R.string.tastiera_1, R.id.group_tastiera)
            createAndSetJsonArray(db, R.array.tastiera2String, generatePositiveUniqueId("tastiera2"), R.string.tastiera_2, R.id.group_tastiera)
            createAndSetJsonArray(db, R.array.tastiera3String, generatePositiveUniqueId("tastiera3"), R.string.tastiera_3, R.id.group_tastiera)
            createAndSetJsonArray(db, R.array.tastiera4String, generatePositiveUniqueId("tastiera4"), R.string.tastiera_4, R.id.group_tastiera)
            createAndSetJsonArray(db, R.array.tastiera5String, generatePositiveUniqueId("tastiera5"), R.string.tastiera_5, R.id.group_tastiera)
            createAndSetJsonArray(db, R.array.tastiera6String, generatePositiveUniqueId("tastiera6"), R.string.tastiera_6, R.id.group_tastiera)
            createAndSetJsonArray(db, R.array.tastiera7String, generatePositiveUniqueId("tastiera7"), R.string.tastiera_7, R.id.group_tastiera)
            createAndSetJsonArray(db, R.array.tastiera8String, generatePositiveUniqueId("tastiera8"), R.string.tastiera_8, R.id.group_tastiera)
            createAndSetJsonArray(db, R.array.tastiera9String, generatePositiveUniqueId("tastiera9"), R.string.tastiera_9, R.id.group_tastiera)
            createAndSetJsonArray(db, R.array.tastiera10String, generatePositiveUniqueId("tastiera10"), R.string.tastiera_10, R.id.group_tastiera)

            // Percussioni
            createAndSetJsonArray(db, R.array.percussioni1String, generatePositiveUniqueId("percussioni1"), R.string.percussioni_1, R.id.group_percussioni)

            // Chitarra
            createAndSetJsonArray(db, R.array.chitarra1String, generatePositiveUniqueId("chitarra1"), R.string.chitarra_1, R.id.group_check_chitarra)

            // Voci
            createAndSetJsonArray(db, R.array.voci1String, generatePositiveUniqueId("voci1"), R.string.voci_1, R.id.group_voci)

            // Cori
            createAndSetJsonArray(db, R.array.cori1String, generatePositiveUniqueId("cori1"), R.string.cori_1, R.id.group_cori)


            Log.i("msg", "applico dati iniziali predefiniti")

            db.close()

            // Imposta isFirstRun a false per indicare che le preferenze sono state impostate
            sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
            sharedPreferences.edit().putBoolean(getString(R.string.dimic_attivo), true).apply()
        }
    }

    private fun generatePositiveUniqueId(input: String): Int {
        val hashCode = input.hashCode()
        // Assicura che il valore sia positivo
        return hashCode and 0x7FFFFFFF
    }


    private fun createAndSetJsonArray(
        db: SQLiteDatabase,
        @ArrayRes redId: Int,
        @IdRes key: Int,
        @StringRes nomePreset: Int,
        @IdRes nomeGroup: Int
    ) {
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_activate_dimic , menu)
        return true
    }

    // Gestisci l'azione quando un elemento del menu viene selezionato
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.MENU_1 -> {
                val sharedPreferences = getSharedPreferences("preset", Context.MODE_PRIVATE)
                val attivazioneMICDI = sharedPreferences.getBoolean(getString(R.string.dimic_attivo), true)
                if(attivazioneMICDI){
                    Toast.makeText(applicationContext,
                        getString(R.string.scelta_mic_e_di_disattivata), Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(applicationContext,
                        getString(R.string.scelta_mic_e_di_attivata), Toast.LENGTH_SHORT).show()
                }

                sharedPreferences.edit().putBoolean(getString(R.string.dimic_attivo), !attivazioneMICDI).apply()
                true
            }

            R.id.MENU_2 ->{
                if(supportFragmentManager.backStackEntryCount < 2){
                    //info funzionamento app
                    Log.i("msg", "cambio contesto")
                    val t = supportFragmentManager.beginTransaction()
                    t.addToBackStack("info")
                    t.replace(R.id.container_main, FragInformazioni())
                    t.commit()
                }
                true

            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}