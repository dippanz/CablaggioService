package com.example.cablaggioservice

import android.content.ContentValues
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.annotation.IdRes
import org.json.JSONArray

class DBMSBoundary(context: Context) {

    private val mappaValori: HashMap<String, List<String>> = HashMap()
    private val dbHelper = MyDatabaseHelper(context)

    fun updateDataOnDB(
        id: String,
        nomePreset: String,
        stringList: List<String>
    ) {

        val db = dbHelper.writableDatabase

        //aggiorno anche mappa di valori
        val listUpdate = if(mappaValori.containsKey(id)){
            mappaValori.getValue(id).toMutableList()
        }else{
            mutableListOf()
        }

        val jsonString = jsonArraytojsonString(stringList)
        val values = ContentValues().apply {
            if(nomePreset.isNotEmpty()){
                put(MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_PRESET, nomePreset)
                Log.i("msg", "titolo non vuoto inserisco valore")

                listUpdate[0] = nomePreset
            }

            if(stringList.isNotEmpty()){
                Log.i("msg", "lista non vuota inserisco valori")
                put(MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_ARRAY, jsonString)

                listUpdate[1] = jsonString
            }
        }

        // Esegui l'aggiornamento
        val numRowsUpdated = db.update(
            MyDatabaseHelper.FeedReaderContract.FeedEntry.TABLE_NAME,
            values, // Valori da aggiornare
            MyDatabaseHelper.FeedReaderContract.FeedEntry.ID + " = ?", // Clausola WHERE per identificare la riga da aggiornare
            arrayOf(id) // Valori dei segnaposto nella clausola WHERE
        )

        // Chiudi il database
        db.close()

        // Controlla il numero di righe aggiornate
        if (numRowsUpdated < 1) {
            Log.i("msg", "AGGIORNAMENTO FALLITO")
        }else if(listUpdate.size >= 2){
            //aggiorno anche mappa di valori
            mappaValori[id] = listUpdate
        }
    }

    fun deletePreset(id: Int) {
        if(id > 0){
            val db = dbHelper.writableDatabase

            val deletedRows = db.delete(MyDatabaseHelper.FeedReaderContract.FeedEntry.TABLE_NAME,
                MyDatabaseHelper.FeedReaderContract.FeedEntry.ID + " = ?",
                arrayOf(id.toString()))

            if (deletedRows <= 0 || deletedRows > 1) {
                Log.i("msg", "ERRORE ELIMINAZIONE PRESET")
            }

            db.close()
        }
        else{
            Log.i("msg", "ERRORE: ID PRESET DA ELIMINARE NON VALIDO")
        }
    }

    fun insertNewPresetOnDB(randomId: Int, nomePreset: String, stringList: List<String>, @IdRes idGroup: Int) {
        val jsonString = jsonArraytojsonString(stringList)

        val values = ContentValues().apply {
            put(MyDatabaseHelper.FeedReaderContract.FeedEntry.ID, randomId)
            put(MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_PRESET, nomePreset)
            put(MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_ARRAY, jsonString)
            put(MyDatabaseHelper.FeedReaderContract.FeedEntry.GROUPS, idGroup)
        }

        val db = dbHelper.writableDatabase

        //newRowId conterrà l'ID della nuova riga inserita, o -1 se si è verificato un errore durante l'inserimento
        val newRowId = db.insert(MyDatabaseHelper.FeedReaderContract.FeedEntry.TABLE_NAME, null, values)

        if(newRowId < 0){
            Log.i("msg", "ERRORE INSERIMENTO VALORI QUERY")
        }else{
            Log.i("msg", "QUERY INSERITA CORRETTAMENTE")
        }

    }

    private fun jsonArraytojsonString(stringArray: List<String>): String {
        //converto stringArray in jsonString per salvare l'intero array
        val jsonArray = JSONArray(stringArray)
        return jsonArray.toString()
    }

    /**
     * se la lista non è gia presente nella mappa la va a recuperare e la setta nella mappa
     * @return lista di strumenti
     */
    fun getStringArrayFromDB(idStringArrayStrumento: String): List<String> {

        //apro db
        val db = dbHelper.readableDatabase

        if(mappaValori.containsKey(idStringArrayStrumento)){
            val jsonString = mappaValori.getValue(idStringArrayStrumento)[1]
            db.close()
            Log.i("msg", "prendo dato $idStringArrayStrumento da mappa")
            return jsonStringtojsonArray(jsonString).toList()
        }else {
            // Definisci una proiezione che specifica quali colonne vuoi recuperare
            val projection = arrayOf(
                MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_ARRAY,
                MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_PRESET
            )

            // Specifica le altre clausole della query, se necessario
            val selection = "${MyDatabaseHelper.FeedReaderContract.FeedEntry.ID} = ?"
            val selectionArgs = arrayOf(idStringArrayStrumento)

            // Eseguire la query per recuperare i dati
            val cursor = db.query(
                MyDatabaseHelper.FeedReaderContract.FeedEntry.TABLE_NAME, // Tabella da cui recuperare i dati
                projection, // Colonnes
                selection, // Colonna di selezione
                selectionArgs, // Argomenti di selezione
                null, // Raggruppamento delle righe
                null, // Filtro sugli altri gruppi di righe
                null // Ordine di ordinamento
            )

            // Ora puoi scorrere il cursore per ottenere i risultati
            return if (cursor.moveToNext()) {
                val nomePreset =  cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_PRESET))
                val jsonString = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_ARRAY))
                cursor.close()
                db.close()
                val elementToReturn = jsonStringtojsonArray(jsonString).toList()
                mappaValori[idStringArrayStrumento] = listOf(nomePreset,jsonString)
                elementToReturn
            } else {
                listOf()
            }
        }
    }

    private fun jsonStringtojsonArray(jsonString: String): Array<String> {
        val jsonArrayRitorno = JSONArray(jsonString)
        return Array(jsonArrayRitorno.length()) { jsonArrayRitorno.getString(it) }
    }

    fun getNomePresetFromDB(view: View): CharSequence {
        val idStringArrayStrumento: String = view.id.toString()

        //apro db
        val db = dbHelper.readableDatabase

        if(mappaValori.containsKey(idStringArrayStrumento)){
            val nomePreset = mappaValori.getValue(idStringArrayStrumento)[0]
            db.close()
            Log.i("msg", "prendo dato $idStringArrayStrumento da mappa")
            return nomePreset
        }else {
            // Definisci una proiezione che specifica quali colonne vuoi recuperare
            val projection = arrayOf(
                MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_ARRAY,
                MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_PRESET
            )

            // Specifica le altre clausole della query, se necessario
            val selection = "${MyDatabaseHelper.FeedReaderContract.FeedEntry.ID} = ?"
            val selectionArgs = arrayOf(idStringArrayStrumento)

            // Eseguire la query per recuperare i dati
            val cursor = db.query(
                MyDatabaseHelper.FeedReaderContract.FeedEntry.TABLE_NAME, // Tabella da cui recuperare i dati
                projection, // Colonnes
                selection, // Colonna di selezione
                selectionArgs, // Argomenti di selezione
                null, // Raggruppamento delle righe
                null, // Filtro sugli altri gruppi di righe
                null // Ordine di ordinamento
            )

            // Ora puoi scorrere il cursore per ottenere i risultati
            return if (cursor.moveToNext()) {
                val nomePreset =  cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_PRESET))
                val jsonString = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_ARRAY))
                cursor.close()
                db.close()
                mappaValori[idStringArrayStrumento] = listOf(nomePreset,jsonString)
                nomePreset
            } else {
                ""
            }
        }

    }

    /**
     * @return lista contentenente lista di stringhe dove il primo valore è l'id e il secondo il
     * nome preset
     */
    fun createRadioButtonFromDB(radioGroup: RadioGroup): List<List<String>>{
        //apro db
        val db = dbHelper.readableDatabase

        // Definisci una proiezione che specifica quali colonne vuoi recuperare
        val projection = arrayOf(
            MyDatabaseHelper.FeedReaderContract.FeedEntry.ID,
            MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_ARRAY,
            MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_PRESET,
            MyDatabaseHelper.FeedReaderContract.FeedEntry.GROUPS
        )

        // Specifica le altre clausole della query, se necessario
        val selection = "${MyDatabaseHelper.FeedReaderContract.FeedEntry.GROUPS} = ?"
        val selectionArgs = arrayOf(radioGroup.id.toString())

        // Eseguire la query per recuperare i dati
        val cursor = db.query(
            MyDatabaseHelper.FeedReaderContract.FeedEntry.TABLE_NAME, // Tabella da cui recuperare i dati
            projection, // Colonnes
            selection, // Colonna di selezione
            selectionArgs, // Argomenti di selezione
            null, // Raggruppamento delle righe
            null, // Filtro sugli altri gruppi di righe
            MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_PRESET + " ASC" // Ordine di ordinamento (ASC per ascendente)
        )

        val mutableList = mutableListOf<List<String>>()

        // Ora puoi scorrere il cursore per ottenere i risultati
        while (cursor.moveToNext()){
            val id =  cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.FeedReaderContract.FeedEntry.ID))
            val nomePreset = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_PRESET))
            val jsonString = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_ARRAY))
            val nomeGroup = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.FeedReaderContract.FeedEntry.GROUPS))

            mutableList.add(listOf(id, nomePreset))
            mappaValori[id] = listOf(nomePreset, jsonString, nomeGroup)
        }

        cursor.close()
        db.close()

        return mutableList
    }

    fun createCheckBoxFromDB(groupCheckChitarra: LinearLayout): List<List<String>>{
        //apro db
        val db = dbHelper.readableDatabase

        // Definisci una proiezione che specifica quali colonne vuoi recuperare
        val projection = arrayOf(
            MyDatabaseHelper.FeedReaderContract.FeedEntry.ID,
            MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_ARRAY,
            MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_PRESET,
            MyDatabaseHelper.FeedReaderContract.FeedEntry.GROUPS
        )

        // Specifica le altre clausole della query, se necessario
        val selection = "${MyDatabaseHelper.FeedReaderContract.FeedEntry.GROUPS} = ?"
        val selectionArgs = arrayOf(groupCheckChitarra.id.toString())

        // Eseguire la query per recuperare i dati
        val cursor = db.query(
            MyDatabaseHelper.FeedReaderContract.FeedEntry.TABLE_NAME, // Tabella da cui recuperare i dati
            projection, // Colonnes
            selection, // Colonna di selezione
            selectionArgs, // Argomenti di selezione
            null, // Raggruppamento delle righe
            null, // Filtro sugli altri gruppi di righe
            MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_PRESET + " ASC" // Ordine di ordinamento (ASC per ascendente)
        )

        val mutableList = mutableListOf<List<String>>()

        // Ora puoi scorrere il cursore per ottenere i risultati
        while (cursor.moveToNext()){
            val id =  cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.FeedReaderContract.FeedEntry.ID))
            val nomePreset = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_PRESET))
            val jsonString = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_ARRAY))
            val nomeGroup = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.FeedReaderContract.FeedEntry.GROUPS))

            mutableList.add(listOf(id, nomePreset))
            mappaValori[id] = listOf(nomePreset, jsonString, nomeGroup)
        }

        cursor.close()
        db.close()

        return mutableList
    }

}