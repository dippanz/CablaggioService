package com.example.cablaggioservice

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.example.cablaggioservice.databinding.FragSezioniBinding
import java.io.File
import kotlin.math.abs
import kotlin.random.Random

class FragSezioni: Fragment(R.layout.frag_sezioni) {

    companion object {
        private const val MAX_WIDTH = 150
        private const val MAX_WIDTH_TABLET = 250
        private const val DIMENSIONE_TABLET_MINIMA = 600
        private const val MAX_HEIGHT = 130
        private const val NAME_BATTERIA = "BATTERIA"
        private const val NAME_PERCUSSIONI = "PERCUSSIONI"
        private const val NAME_TASTIERA = "TASTIERA"
        private const val NAME_VOCI = "VOCI"
        private const val NAME_CORI = "CORI"
        private const val NAME_CHITARRA = "GTR"
        private const val NAME_BASSO = "BASSO"
        private const val NAME_SAX = "SAX"
        private const val TYPE_MIC = "MIC"
        private const val TYPE_DI = "D.I."
        private const val MAX_TITLE_PRESET = 30
        private const val DURATA_ANIMAZIONE = 400L
        private const val TEXT_SIZE = 15f
        private const val SCALE_RADIO_BUTTON = 0.85f
    }

    /**
     * questa mappa contiene come key l'ID dello strumento
     * invece il value è una lista contente come primo elemento
     * il nome del preset, come secondo elemento un jsonString che rappresenta
     * l'array contente i singoli strumenti, come terzo elemento l'id del group a cui appartiene
     */
    private lateinit var dbmsBoundary: DBMSBoundary

    private fun performSwipeAnimation(view: View, endPosition: Float) {
        // Aggiungi un'animazione di indicatore visivo
        val shakeAnimation = AnimationUtils.loadAnimation(context, R.anim.animation_scorrimento)
        view.startAnimation(shakeAnimation)

        val animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, endPosition)
        animator.duration = DURATA_ANIMAZIONE
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Rimuovi l'EditText dal layout dopo l'animazione
                val parentView = view.parent as? View
                parentView?.let {
                    (it as? ViewGroup)?.removeView(view)
                }

                if(parentView is LinearLayout){
                    for((index, e) in parentView.children.withIndex()){
                        if(e.id != index){
                            e.id = index
                        }
                    }
                }
            }
        })

        animator.start()
    }

    private lateinit var binding: FragSezioniBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragSezioniBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbmsBoundary = DBMSBoundary(requireContext())

        //setto tutti i popup per info singoli preset
        val radioGroups = mutableListOf<RadioGroup>()
        radioGroups.add(binding.groupBatteria)
        radioGroups.add(binding.groupCori)
        radioGroups.add(binding.groupPercussioni)
        radioGroups.add(binding.groupVoci)
        radioGroups.add(binding.groupTastiera)

        // Itera attraverso tutti i RadioGroup per creare i vari preset
        for (radioGroup in radioGroups) {
            val list = dbmsBoundary.createRadioButtonFromDB(radioGroup)
            for(element in list){
                val nomePreset = element[1]
                val id = element[0]

                //creo radioButton con specifiche sopra elencate
                val radioButton = settingRadioButton(nomePreset, id)
                radioGroup.addView(radioButton)
                setOnLongListenerPopup(radioButton)
            }
        }

        //crea preset chitarra
        val list = dbmsBoundary.createCheckBoxFromDB(binding.groupCheckChitarra)
        for(element in list){
            val nomePreset = element[1]
            val id = element[0]

            //creo checkbox con specifiche sopra elencate
            val checkBox = settingCheckBox(nomePreset, id)
            binding.groupCheckChitarra.addView(checkBox)
            setOnLongListenerPopup(checkBox)
        }

        setOnLongListenerCreatePreset(binding.checkboxBatteria)
        setOnLongListenerCreatePreset(binding.checkBoxPercussioni)
        setOnLongListenerCreatePreset(binding.checkboxChitarra)
        setOnLongListenerCreatePreset(binding.checkBoxTastiera)
        setOnLongListenerCreatePreset(binding.checkBoxVoci)
        setOnLongListenerCreatePreset(binding.checkBoxCori)

        binding.buttonCaricaCanali.setOnClickListener {

            var validModel = true
            //lista completa
            val modelList: ArrayList<ModelChannelList> = ArrayList()
            //aggiunto per evitare errore outofbound da rimuovere alla fine
            modelList.add(ModelChannelList("0","","","","","","","","",""))

            //carico canali batteria
            if(binding.checkboxBatteria.isChecked){
                val idStringArrayStrumento: String = if(binding.groupBatteria.checkedRadioButtonId > 0){
                    binding.groupBatteria.checkedRadioButtonId.toString()
                }else{
                    validModel = false
                    Toast.makeText(context, getString(R.string.inserire_pre_set, "batteria"), Toast.LENGTH_SHORT).show()
                    ""
                }

                val stringArray = dbmsBoundary.getStringArrayFromDB(idStringArrayStrumento)
                Log.i("msg", stringArray.toString())

                //creo i modelli batteria
                for(i in stringArray.indices){
                    val model = if(i == 0){
                        ModelChannelList("1", NAME_BATTERIA, stringArray[i].split(" %")[0],TYPE_MIC, "","","","","","")
                    }else{
                        ModelChannelList("${i.plus(1)}", "", stringArray[i].split(" %")[0],TYPE_MIC, "","","","","","")
                    }
                    modelList.add(model)
                }
            }

            //carico canali percussioni
            if(binding.checkBoxPercussioni.isChecked){

                val idStringArrayStrumento: String = if(binding.groupPercussioni.checkedRadioButtonId > 0){
                    binding.groupPercussioni.checkedRadioButtonId.toString()
                }else{
                    validModel = false
                    Toast.makeText(context, getString(R.string.inserire_pre_set, "percussioni"), Toast.LENGTH_SHORT).show()
                    ""
                }

                val stringArray = dbmsBoundary.getStringArrayFromDB(idStringArrayStrumento)
                Log.i("msg", stringArray.toString())

                val range = IntRange(modelList[modelList.lastIndex].id.toInt(), stringArray.size + modelList[modelList.lastIndex].id.toInt() - 1)
                //creo i modelli
                for((i, id) in range.withIndex()){
                    val model = if(i == 0){
                        ModelChannelList("${id.plus(1)}", NAME_PERCUSSIONI, stringArray[i].split(" %")[0], stringArray[i].split(" %")[1], "","","","","","")
                    }else{
                        ModelChannelList("${id.plus(1)}", "", stringArray[i].split(" %")[0],stringArray[i].split(" %")[1], "","","","","","")
                    }
                    modelList.add(model)
                }
            }

            //carico canali basso
            if(binding.checkBoxBasso.isChecked){
                val model = ModelChannelList("${modelList[modelList.lastIndex].id.toInt().plus(1)}", NAME_BASSO, NAME_BASSO.lowercase(), TYPE_DI,
                    "","","","","","")
                modelList.add(model)

            }

            //carico canali tastiera
            if(binding.checkBoxTastiera.isChecked){

                val idStringArrayStrumento: String = if(binding.groupTastiera.checkedRadioButtonId > 0){
                    binding.groupTastiera.checkedRadioButtonId.toString()
                }else{
                    validModel = false
                    Toast.makeText(context, getString(R.string.inserire_pre_set, "tastiera"), Toast.LENGTH_SHORT).show()
                    ""
                }

                val stringArray = dbmsBoundary.getStringArrayFromDB(idStringArrayStrumento)

                Log.i("msg", stringArray.toString())

                val range = IntRange(modelList[modelList.lastIndex].id.toInt(), stringArray.size + modelList[modelList.lastIndex].id.toInt() - 1)
                //creo i modelli batteria
                for((i, id) in range.withIndex()){
                    val model = if(i == 0){
                        ModelChannelList("${id.plus(1)}", NAME_TASTIERA, stringArray[i].split(" %")[0], stringArray[i].split(" %")[1], "","","","","","")
                    }else{
                        ModelChannelList("${id.plus(1)}", "", stringArray[i].split(" %")[0],stringArray[i].split(" %")[1], "","","","","","")
                    }
                    modelList.add(model)
                }
            }

            //carico canali CHITARRA
            if(binding.checkboxChitarra.isChecked){
                var indexGuitar = 1
                for(e in binding.groupCheckChitarra.children){
                    if(e is CheckBox && e.isChecked){

                        val idStringArrayStrumento: String = e.id.toString()

                        val stringArray = dbmsBoundary.getStringArrayFromDB(idStringArrayStrumento)

                        val range = IntRange(modelList[modelList.lastIndex].id.toInt(), stringArray.size + modelList[modelList.lastIndex].id.toInt() - 1)
                        //creo i modelli batteria
                        for((i, id) in range.withIndex()){
                            val model = if(i == 0){
                                ModelChannelList("${id.plus(1)}",
                                    "$NAME_CHITARRA $indexGuitar", stringArray[i].split(" %")[0], stringArray[i].split(" %")[1], "","","","","","")
                            }else{
                                ModelChannelList("${id.plus(1)}", "", stringArray[i].split(" %")[0],stringArray[i].split(" %")[1], "","","","","","")
                            }
                            modelList.add(model)
                        }
                        indexGuitar++
                    }
                }

                if(indexGuitar == 1){
                    validModel = false
                    Toast.makeText(context, getString(R.string.scegliere_pre_set, "chitarra"), Toast.LENGTH_SHORT).show()
                }
            }

            //carico canali sax
            if(binding.checkBoxSax.isChecked){
                val model = ModelChannelList("${modelList[modelList.lastIndex].id.toInt().plus(1)}", NAME_SAX, NAME_SAX.lowercase(), TYPE_MIC,
                    "","","","","","")
                modelList.add(model)

            }

            //carico canali VOCI
            if(binding.checkBoxVoci.isChecked){

                val idStringArrayStrumento: String = if(binding.groupVoci.checkedRadioButtonId > 0){
                    binding.groupVoci.checkedRadioButtonId.toString()
                }else{
                    validModel = false
                    Toast.makeText(context, getString(R.string.inserire_pre_set, "voci"), Toast.LENGTH_SHORT).show()
                    ""
                }

                val stringArray = dbmsBoundary.getStringArrayFromDB( idStringArrayStrumento)
                Log.i("msg", stringArray.toString())

                val range = IntRange(modelList[modelList.lastIndex].id.toInt(), stringArray.size + modelList[modelList.lastIndex].id.toInt() - 1)
                //creo i modelli VOCI
                for((i, id) in range.withIndex()){
                    val model = if(i == 0){
                        ModelChannelList("${id.plus(1)}", NAME_VOCI, stringArray[i].split(" %")[0], TYPE_MIC, "","","","","","")
                    }else{
                        ModelChannelList("${id.plus(1)}", "", stringArray[i].split(" %")[0],TYPE_MIC, "","","","","","")
                    }
                    modelList.add(model)
                }
            }

            //carico canali CORI
            if(binding.checkBoxCori.isChecked){

                val idStringArrayStrumento: String = if(binding.groupCori.checkedRadioButtonId > 0){
                    binding.groupCori.checkedRadioButtonId.toString()
                }else{
                    validModel = false
                    Toast.makeText(context, getString(R.string.inserire_pre_set, "cori"), Toast.LENGTH_SHORT).show()
                    ""
                }

                val stringArray = dbmsBoundary.getStringArrayFromDB(idStringArrayStrumento)

                val range = IntRange(modelList[modelList.lastIndex].id.toInt(), stringArray.size + modelList[modelList.lastIndex].id.toInt() - 1)
                //creo i modelli CORI
                for((i, id) in range.withIndex()){
                    val model = if(i == 0){
                        ModelChannelList("${id.plus(1)}", NAME_CORI, stringArray[i].split(" %")[0], TYPE_MIC, "","","","","","")
                    }else{
                        ModelChannelList("${id.plus(1)}", "", stringArray[i].split(" %")[0],TYPE_MIC, "","","","","","")
                    }
                    modelList.add(model)
                }
            }

            if(validModel && (binding.checkBoxVoci.isChecked ||
                binding.checkboxBatteria.isChecked ||
                binding.checkBoxCori.isChecked ||
                binding.checkboxChitarra.isChecked ||
                binding.checkBoxPercussioni.isChecked ||
                binding.checkBoxSax.isChecked ||
                binding.checkBoxBasso.isChecked ||
                binding.checkBoxTastiera.isChecked)){
                //esporto file excel
                val fileName = "cablaggi.xlsx"
                CreatorExcelFile(requireContext(), fileName, modelList.subList(1, modelList.size)).createExcelFile()

                //creazione file
                val file = File(requireContext().filesDir, fileName)
                val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)

                Log.i("msgE", file.canRead().toString())

                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                if (intent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(intent)
                } else {
                    // Gestisci il caso in cui l'app di terze parti non sia installata
                    // Mostra un messaggio all'utente o gestisci la situazione in altro modo
                    Toast.makeText(requireContext(), "L'applicazione per aprire i file Excel non è installata.", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(requireContext(), "selezionare almeno una delle voci disponibili", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun settingCheckBox(nomePreset: String, id: String): CheckBox{
        val checkBox = CheckBox(requireContext())
        checkBox.id = id.toInt()
        checkBox.textSize = TEXT_SIZE
        checkBox.text = nomePreset
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        checkBox.layoutParams = layoutParams
        return checkBox
    }

    private fun settingRadioButton(nomePreset: String, id: String): RadioButton{
        val radioButton = RadioButton(requireContext())
        radioButton.id = id.toInt()
        radioButton.textSize = TEXT_SIZE
        radioButton.text = nomePreset
        val layoutParams = RadioGroup.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        radioButton.layoutParams = layoutParams
        return radioButton
    }

    private var attivazioneMICDI: Boolean = false

    /**
     * funzione che permette di associare popup modifiche per i singoli preset di ogni gruppo
     */
    private fun setOnLongListenerPopup(view: View){
        if(view is RadioButton || view is CheckBox){
            // Verifica se l'elemento è un RadioButton
            view.setOnLongClickListener {

                attivazioneMICDI = when((view.parent as ViewGroup).id){
                    R.id.group_check_chitarra-> true
                   R.id.group_percussioni-> true
                   R.id.group_tastiera-> true
                   else-> false
                }

                val idStringArrayStrumento: String = view.id.toString()

                //recupero dati
                val stringArray = dbmsBoundary.getStringArrayFromDB(idStringArrayStrumento)

                //creo stringa contenente array incolonnato
                val wordList = createStringIncolonnata(stringArray)

                //creo popup informativo
                val alertDialogBuilder = AlertDialog.Builder(requireContext(), R.style.RoundedCornersDialog)
                alertDialogBuilder.setTitle(getString(R.string.info_1s, dbmsBoundary.getNomePresetFromDB(view)))
                alertDialogBuilder.setMessage(wordList)

                alertDialogBuilder.setNeutralButton("Modifica"){ _, _ ->

                    val dialogBuilder = Dialog(requireContext(), R.style.RoundedCornersDialog)
                    dialogBuilder.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialogBuilder.setCancelable(true)

                    val rootView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_dialog_modifica, null)
                    dialogBuilder.setContentView(rootView)

                    val titleEditText = rootView.findViewById<EditText>(R.id.textViewTitle)
                    val filterArray = arrayOf<InputFilter>(InputFilter.LengthFilter(MAX_TITLE_PRESET))
                    titleEditText.filters = filterArray
                    titleEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    titleEditText.hint = getString(R.string.modifica_preset_s_1, dbmsBoundary.getNomePresetFromDB(view))
                    /*titleEditText.addTextChangedListener(object : TextWatcher {

                        private val startText = titleEditText.text.toString().split(": ")[0] + ": "

                        override fun beforeTextChanged(
                            p0: CharSequence?,
                            p1: Int,
                            p2: Int,
                            p3: Int
                        ) {
                            //Log.i("msg", startText)
                        }

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            if (s != null) {
                                if (s.endsWith(": ")) {
                                    titleEditText.setSelection(s.indexOf(": ") + 2)
                                }
                            }
                        }

                        override fun afterTextChanged(editable: Editable?) {
                            val text = editable.toString()

                            if (!text.startsWith(startText)) {
                                editable?.replace(0, editable.length, startText)

                            }

                            // Controlla se l'utente sta cercando di cancellare i due punti
                            if (text == startText) {
                                editable?.insert(startText.length, " ")
                            }
                        }
                    })*/

                    val editTextContainer = rootView.findViewById<LinearLayout>(R.id.container_editText)

                    val wordListWithMICDI = createStringIncolonnata(stringArray, true)
                    if(wordListWithMICDI.isNotEmpty()) {
                        // Crea caselle di testo EditText in base al numero di parole nella lista
                        for ( word in wordListWithMICDI.split("\n")) {
                            addRowOnContainer(editTextContainer, word)
                        }
                    }

                    val saveButton = rootView.findViewById<Button>(R.id.buttonSalva)
                    val cancelButton = rootView.findViewById<Button>(R.id.buttonAnnulla)
                    val addButton = rootView.findViewById<Button>(R.id.buttonAggiungi)

                    saveButton.setOnClickListener {
                        //prendo il nuovo nomePreset
                        val nomePreset: String = titleEditText.text.toString().ifEmpty {
                            titleEditText.hint.toString()
                        }



                        //salvo il nuovo nome del preset sia in locale che dinamicamente se diverso da quello precedente
                        val titoloPreset: String = if(dbmsBoundary.getNomePresetFromDB(view) != nomePreset){
                            if(view is RadioButton){
                                view.text = nomePreset
                            }else if(view is CheckBox){
                                view.text = nomePreset
                            }
                            nomePreset
                        }else{
                            ""
                        }

                        val stringList = saveModifichePopup(editTextContainer)

                        //controllo che il titolo o almeno un dato sia stato modificato
                        if(stringList.isNotEmpty() && stringList != stringArray){
                            Log.i("msg", "array diversi allora carico dati")
                            //aggiorno dati nel db
                            dbmsBoundary.updateDataOnDB(idStringArrayStrumento, titoloPreset, stringList)
                        }else if(titoloPreset.isNotEmpty() && stringList.isNotEmpty()){
                            Log.i("msg", "solo titolo diverso carico dati")
                            dbmsBoundary.updateDataOnDB(idStringArrayStrumento, titoloPreset, listOf())
                        }else{
                            if(stringList.isEmpty()){
                                Toast.makeText(requireContext(), "aggiungere valori mancanti", Toast.LENGTH_SHORT).show()
                            }else{
                                Toast.makeText(requireContext(), "non è stato cambiato nessun dato", Toast.LENGTH_SHORT).show()
                            }

                            return@setOnClickListener
                        }

                        dialogBuilder.dismiss()
                    }

                    cancelButton.setOnClickListener {
                        dialogBuilder.dismiss() // Chiudi il Dialog senza salvare
                    }

                    addButton.setOnClickListener {
                        addRowOnContainer(editTextContainer, getString(R.string.aggiungi_un_valore))
                    }

                    dialogBuilder.show()
                }

                alertDialogBuilder.setPositiveButton("Chiudi") { dialog, _ ->
                    // Azioni da eseguire quando si fa clic su OK
                    dialog.dismiss()
                }

                alertDialogBuilder.setNegativeButton("Elimina"){ _: DialogInterface, _: Int ->

                    //creo popup informativo
                    val alertConfirmElimination = AlertDialog.Builder(requireContext())
                    alertConfirmElimination.setTitle("Conferma eliminazione")
                        .setMessage(getString(R.string.sicuro_di_voler_eliminare, dbmsBoundary.getNomePresetFromDB(view)))
                        .setNegativeButton("Annulla"){ dialog: DialogInterface, _: Int ->
                            dialog.dismiss()
                        }
                        .setPositiveButton("Si"){ dialog: DialogInterface, _: Int ->
                            //cancello view dinamicamente
                            val parentPreset = view.parent
                            parentPreset?.let {
                                (it as? ViewGroup)?.removeView(view)
                            }

                            //cancello view dal db
                            dbmsBoundary.deletePreset(view.id)

                            dialog.dismiss()
                        }.create().show()
                }

                val alertDialog = alertDialogBuilder.create()

                // Ottieni i pulsanti dal dialog e applica lo stile personalizzato
                alertDialog.setOnShowListener { dialog ->
                    val positiveButton = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
                    val neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                    val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

                    // Imposta il colore del testo dei pulsanti
                    positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.custom_button_background_color))
                    negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.custom_button_background_color))
                    neutralButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.custom_button_background_color))

                }
                alertDialog.show()

                true
            }

        }else{
            Log.i("msg", "errore setOnLongClick radiogroup")
        }
    }

    private fun saveModifichePopup(editTextContainer: LinearLayout): List<String>{
        val stringList = mutableListOf<String>()
        //salvare eventuali modifiche
        for(e in editTextContainer.children){
            if(e is LinearLayout ){
                val editText = e.getChildAt(0) as EditText

                val sharedPreferences = requireContext().getSharedPreferences("preset", Context.MODE_PRIVATE)
                val attivazioneMICDItmp = sharedPreferences.getBoolean(getString(R.string.dimic_attivo), true)

                if(attivazioneMICDI && attivazioneMICDItmp){
                    val radioGroup = e.getChildAt(1) as RadioGroup

                    if(editText.length() != 0 && radioGroup.checkedRadioButtonId != -1) {
                        val testoRadioButton = (radioGroup.findViewById<RadioButton>(radioGroup.checkedRadioButtonId)).text.toString()
                        stringList.add("${editText.text} %$testoRadioButton")

                    }else if(editText.length() != 0){
                        stringList.add("${editText.text} %MIC")

                    }else if(radioGroup.checkedRadioButtonId != -1){
                        val testoRadioButton = (radioGroup.findViewById<RadioButton>(radioGroup.checkedRadioButtonId)).text.toString()
                        stringList.add("${editText.hint} %$testoRadioButton")
                    }

                }else if(editText.length() != 0){
                    stringList.add("${editText.text} %MIC")
                }else{
                    stringList.add("${editText.hint} %MIC")
                }

                //controllo se non sia un nuovo valore
                if(editText.text.isEmpty() && editText.hint.toString() == getString(R.string.aggiungi_un_valore)){
                    //Toast.makeText(requireContext(), "inserire un valore per i nuovi valori aggiunti", Toast.LENGTH_SHORT).show()
                    return listOf()
                }

            }
        }

        return stringList
    }


    /**
     * funzione che permette di associare popup creazione preset
     */
    private fun setOnLongListenerCreatePreset(checkBox: CheckBox){

        // Verifica se l'elemento è un RadioButton
        checkBox.setOnLongClickListener {

            attivazioneMICDI = when(checkBox.id){
                R.id.checkboxChitarra -> true
                R.id.checkBoxTastiera-> true
                R.id.checkBoxPercussioni-> true
                else-> false
            }

            val dialogBuilder = Dialog(requireContext(), R.style.RoundedCornersDialog)
            dialogBuilder.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialogBuilder.setCancelable(true)

            val rootView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_dialog_modifica, null)
            dialogBuilder.setContentView(rootView)

            val titleEditText = rootView.findViewById<EditText>(R.id.textViewTitle)
            val filterArray = arrayOf<InputFilter>(InputFilter.LengthFilter(MAX_TITLE_PRESET))
            titleEditText.filters = filterArray
            titleEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE

            titleEditText.hint = getString(R.string.nome_preset_s_1)
            /*titleEditText.addTextChangedListener(object : TextWatcher {

                        private val startText = titleEditText.text.toString().split(": ")[0] + ": "

                        override fun beforeTextChanged(
                            p0: CharSequence?,
                            p1: Int,
                            p2: Int,
                            p3: Int
                        ) {
                            //Log.i("msg", startText)
                        }

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            if (s != null) {
                                if (s.endsWith(": ")) {
                                    titleEditText.setSelection(s.indexOf(": ") + 2)
                                }
                            }
                        }

                        override fun afterTextChanged(editable: Editable?) {
                            val text = editable.toString()

                            if (!text.startsWith(startText)) {
                                editable?.replace(0, editable.length, startText)

                            }

                            // Controlla se l'utente sta cercando di cancellare i due punti
                            if (text == startText) {
                                editable?.insert(startText.length, " ")
                            }
                        }
                    })*/

                    val editTextContainer = rootView.findViewById<LinearLayout>(R.id.container_editText)

                    val saveButton = rootView.findViewById<Button>(R.id.buttonSalva)
                    val cancelButton = rootView.findViewById<Button>(R.id.buttonAnnulla)
                    val addButton = rootView.findViewById<Button>(R.id.buttonAggiungi)

                    saveButton.setOnClickListener {
                        //prendo il nuovo nomePreset
                        val nomePreset = titleEditText.text.toString()

                        val stringList = saveModifichePopup(editTextContainer)

                        if(nomePreset.isEmpty()){
                            Toast.makeText(requireContext(), "inserire un nome preset", Toast.LENGTH_SHORT).show()
                        }else if(stringList.isEmpty()) {
                            Toast.makeText(requireContext(), "inserire i dati", Toast.LENGTH_SHORT).show()
                        }else{
                            val systemTimeMillis = System.currentTimeMillis()
                            val viewId = {
                                val random = Random(systemTimeMillis)
                                random.nextInt(Integer.MAX_VALUE)
                            }

                            val idGroup = when(checkBox.id){
                                R.id.checkboxBatteria -> { R.id.group_batteria }
                                R.id.checkBoxPercussioni -> { R.id.group_percussioni }
                                R.id.checkboxChitarra -> { R.id.group_check_chitarra }
                                R.id.checkBoxVoci -> { R.id.group_voci }
                                R.id.checkBoxTastiera -> { R.id.group_tastiera }
                                R.id.checkBoxCori -> { R.id.group_cori }
                               else ->{-1}
                            }

                            if(idGroup > 0){
                                dbmsBoundary.insertNewPresetOnDB(viewId(),nomePreset, stringList, idGroup)
                            }

                            //creo check o radiobutton
                            if(idGroup == R.id.group_check_chitarra){
                                val checkBoxView = settingCheckBox(nomePreset, viewId().toString())
                                setOnLongListenerPopup(checkBoxView)
                                binding.root.findViewById<LinearLayout>(idGroup).addView(checkBoxView)
                            }else{
                                val radioButton = settingRadioButton(nomePreset, viewId().toString())
                                setOnLongListenerPopup(radioButton)
                                binding.root.findViewById<RadioGroup>(idGroup).addView(radioButton)
                            }

                            dialogBuilder.dismiss()
                        }
                    }

                    cancelButton.setOnClickListener {
                        dialogBuilder.dismiss() // Chiudi il Dialog senza salvare
                    }

                    addButton.setOnClickListener {
                        addRowOnContainer(editTextContainer, getString(R.string.aggiungi_un_valore))
                    }

                    dialogBuilder.show()

            true
        }
    }

    /**
     * funzione che aggiunge una riga al container, ogni riga è composta da una linearLayout orizzontale
     * e al suo interno sono presenti un editText e un radioGroup con i radioButton relativi
     */
    private fun addRowOnContainer(editTextContainer: LinearLayout, text: String){
        //imposto altezza massima scrollView (parent)
        val maxHeightLinearLayout = 1200
        val scrollView = editTextContainer.parent as View
        val calculatedHeight = scrollView.height
        Log.i("msg", calculatedHeight.toString())

        // Imposta l'altezza del LinearLayout se supera quella massima
        if(calculatedHeight > maxHeightLinearLayout){
            val params = scrollView.layoutParams
            params.height = maxHeightLinearLayout
            scrollView.layoutParams = params
        }

        //contenitore principale per riga
        val tmpContainer = LinearLayout(requireContext())
        tmpContainer.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        tmpContainer.orientation = LinearLayout.HORIZONTAL
        tmpContainer.setPadding(40,1,10,20)

        //primo elemento della riga
        val layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.weight = 1f // Imposta il peso maggiore per la EditText
        val editText = CustomEditText(requireContext())
        editText.id = View.generateViewId()
        val filterArray = arrayOf<InputFilter>(InputFilter.LengthFilter(MAX_TITLE_PRESET))
        editText.filters = filterArray
        editText.setBackgroundColor(
            resources.getColor(
                R.color.trasparente,
                null
            )
        )
        editText.hint = text.split(" %")[0]
        editText.layoutParams = layoutParams
        //aggiungo editText alla riga i-esima
        tmpContainer.addView(editText)

        val sharedPreferences = requireContext().getSharedPreferences("preset", Context.MODE_PRIVATE)
        val attivazioneMICDItmp = sharedPreferences.getBoolean(getString(R.string.dimic_attivo), true)

        if(attivazioneMICDItmp && attivazioneMICDI){
            //secondo elemento della riga
            val radioGroup = RadioGroup(requireContext())
            radioGroup.id = View.generateViewId()
            radioGroup.orientation = LinearLayout.HORIZONTAL
            radioGroup.setPadding(0,16,0,0)

            // Crea due RadioButton
            val radioButton1 = RadioButton(requireContext())
            val radioButton2 = RadioButton(requireContext())

            // Assegna un ID univoco alle RadioButton
            radioButton1.id = View.generateViewId()
            radioButton2.id = View.generateViewId()
            radioButton1.scaleX = SCALE_RADIO_BUTTON
            radioButton1.scaleY = SCALE_RADIO_BUTTON
            radioButton2.scaleX = SCALE_RADIO_BUTTON
            radioButton2.scaleY = SCALE_RADIO_BUTTON

            // Imposta il testo delle RadioButton
            radioButton1.text = getString(R.string.di)
            radioButton2.text = getString(R.string.mic)

            // Aggiungi le RadioButton al LinearLayout
            radioGroup.addView(radioButton1)
            radioGroup.addView(radioButton2)

            val pattern = Regex(".* %[A-Za-z.]+")
            if(pattern.matches(text)){
                //setto radioButton se va selezionato
                val textSelection = text.split(" %")[1]
                if(textSelection == getString(R.string.di)){
                    radioGroup.check(radioButton1.id)
                }else if(textSelection == getString(R.string.mic)){
                    radioGroup.check(radioButton2.id)
                }
            }

            // Aggiungi view alla riga i-esima
            tmpContainer.addView(radioGroup)
        }

        editTextContainer.addView(tmpContainer) // Aggiungi la casella di testo al layout padre
        setScorrimento(tmpContainer)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setScorrimento(linearLayout: LinearLayout) {
        // Imposta il modo di eliminazione con scorrimento
        linearLayout.setOnTouchListener(object : View.OnTouchListener {
            private var startX = 1f
            private val SWIPE_THRESHOLD = 150
            private var deltaX = 0f // Differenza tra la posizione iniziale e la posizione attuale del dito
            private var isSwiping = false // Flag per rilevare lo stato di swipe

            override fun onTouch(view: View, event: MotionEvent): Boolean {

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.x
                        deltaX = event.rawX - view.x
                        isSwiping = false // Resetta il flag quando viene premuto
                        linearLayout.requestFocus() // Imposta il focus sull'EditText
                        linearLayout.performClick()
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        // Calcola la nuova posizione in base alla posizione del dito
                        val newX = event.rawX - deltaX

                        if (newX < SWIPE_THRESHOLD) {
                            // Imposta la nuova posizione della vista
                            view.x = newX
                            isSwiping = true // L'utente sta eseguendo uno swipe
                        } else {
                            view.x += newX * 0.07f
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        val endX = event.x
                        val deltaX = endX - startX

                        // Verifica se la distanza percorsa è sufficiente per considerarlo uno swipe
                        if (abs(deltaX) > SWIPE_THRESHOLD && isSwiping) {
                            if (deltaX > 0) {
                                // Swipe verso destra, elimina l'EditText
                                performSwipeAnimation(view, endX)
                            } else {
                                // Swipe verso sinistra, elimina l'EditText
                                performSwipeAnimation(view, endX)
                            }

                        } else {
                            view.x = 14f
                        }

                        // editText.performClick() // Non è necessario chiamare performClick() qui
                        return true
                    }
                }
                return false
            }
        })
    }


    private fun createStringIncolonnata(stringArray: List<String>, withoutSplit: Boolean = false): String {
        if(stringArray.isEmpty()){
            return ""
        }

        val sb = StringBuilder()
        for(e in stringArray){
            if(withoutSplit){
                sb.append(e + "\n")
            }else{
                sb.append(e.split(" %")[0] + "\n")
            }
        }

        return sb.substring(0, sb.length - 1).toString()
    }


    override fun onStart() {
        super.onStart()

        //setto comportamenti bottoni
        binding.checkboxBatteria.setOnClickListener {
            if(binding.checkboxBatteria.isChecked){
                binding.imageButtonBatteria.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_drop_down_48, null))
                binding.scrollViewBatteria.visibility = View.VISIBLE
                adattaView(binding.scrollViewBatteria)
            }else{
                binding.imageButtonBatteria.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_right_48, null))
                binding.scrollViewBatteria.visibility = View.GONE
                binding.groupBatteria.clearCheck()
            }
        }

        binding.imageButtonBatteria.setOnClickListener {
            if(binding.scrollViewBatteria.visibility == View.VISIBLE){
                binding.imageButtonBatteria.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_right_48, null))
                binding.scrollViewBatteria.visibility = View.GONE
            }else if(binding.checkboxBatteria.isChecked){
                binding.imageButtonBatteria.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_drop_down_48, null))
                binding.scrollViewBatteria.visibility = View.VISIBLE
                adattaView(binding.scrollViewBatteria)
            }
        }

        binding.checkBoxPercussioni.setOnClickListener {
            if(binding.checkBoxPercussioni.isChecked){
                binding.imageButtonPercussioni.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_drop_down_48, null))
                binding.scrollViewPercussioni.visibility = View.VISIBLE
                adattaView(binding.scrollViewPercussioni)
            }else{
                binding.imageButtonPercussioni.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_right_48, null))
                binding.scrollViewPercussioni.visibility = View.GONE
                binding.groupPercussioni.clearCheck()
            }
        }

        binding.imageButtonPercussioni.setOnClickListener {
            if(binding.scrollViewPercussioni.visibility == View.VISIBLE){
                binding.imageButtonPercussioni.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_right_48, null))
                binding.scrollViewPercussioni.visibility = View.GONE
            }else if(binding.checkBoxPercussioni.isChecked){
                binding.imageButtonPercussioni.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_drop_down_48, null))
                binding.scrollViewPercussioni.visibility = View.VISIBLE
                adattaView(binding.scrollViewPercussioni)
            }
        }

        binding.checkBoxTastiera.setOnClickListener {
            if(binding.checkBoxTastiera.isChecked){
                binding.imageButtonTastiera.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_drop_down_48, null))
                binding.scrollViewTastiera.visibility = View.VISIBLE
                adattaView(binding.scrollViewTastiera)
            }else{
                binding.imageButtonTastiera.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_right_48, null))
                binding.scrollViewTastiera.visibility = View.GONE
                binding.groupTastiera.clearCheck()
            }
        }

        binding.imageButtonTastiera.setOnClickListener {
            if(binding.scrollViewTastiera.visibility == View.VISIBLE){
                binding.imageButtonTastiera.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_right_48, null))
                binding.scrollViewTastiera.visibility = View.GONE
            }else if(binding.checkBoxTastiera.isChecked){
                binding.imageButtonTastiera.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_drop_down_48, null))
                binding.scrollViewTastiera.visibility = View.VISIBLE
                adattaView(binding.scrollViewTastiera)
            }
        }

        binding.checkboxChitarra.setOnClickListener {
            if(binding.checkboxChitarra.isChecked){
                binding.imageButtonChitarra.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_drop_down_48, null))
                binding.scrollViewChitarra.visibility = View.VISIBLE
                adattaView(binding.scrollViewChitarra)
            }else{
                binding.imageButtonChitarra.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_right_48, null))
                binding.scrollViewChitarra.visibility = View.GONE
                clearCheckChitarra(binding.scrollViewChitarra.getChildAt(0) as LinearLayout)
            }
        }

        binding.imageButtonChitarra.setOnClickListener {
            if(binding.scrollViewChitarra.visibility == View.VISIBLE){
                binding.imageButtonChitarra.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_right_48, null))
                binding.scrollViewChitarra.visibility = View.GONE
            }else if(binding.checkboxChitarra.isChecked){
                binding.imageButtonChitarra.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_drop_down_48, null))
                binding.scrollViewChitarra.visibility = View.VISIBLE
                adattaView(binding.scrollViewChitarra)
            }
        }

        binding.checkBoxVoci.setOnClickListener {
            if(binding.checkBoxVoci.isChecked){
                binding.imageButtonVoci.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_drop_down_48, null))
                binding.scrollViewVoci.visibility = View.VISIBLE
                adattaView(binding.scrollViewVoci)
            }else{
                binding.imageButtonVoci.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_right_48, null))
                binding.scrollViewVoci.visibility = View.GONE
                binding.groupVoci.clearCheck()
            }
        }

        binding.imageButtonVoci.setOnClickListener {
            if(binding.scrollViewVoci.visibility == View.VISIBLE){
                binding.imageButtonVoci.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_right_48, null))
                binding.scrollViewVoci.visibility = View.GONE
            }else if(binding.checkBoxVoci.isChecked){
                binding.imageButtonVoci.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_drop_down_48, null))
                binding.scrollViewVoci.visibility = View.VISIBLE
                adattaView(binding.scrollViewVoci)
            }
        }

        binding.checkBoxCori.setOnClickListener {
            if(binding.checkBoxCori.isChecked){
                binding.imageButtonCori.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_drop_down_48, null))
                binding.scrollViewCori.visibility = View.VISIBLE
                adattaView(binding.scrollViewCori)
            }else{
                binding.imageButtonCori.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_right_48, null))
                binding.scrollViewCori.visibility = View.GONE
                binding.groupCori.clearCheck()
            }
        }

        binding.imageButtonCori.setOnClickListener {
            if(binding.scrollViewCori.visibility == View.VISIBLE){
                binding.imageButtonCori.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_right_48, null))
                binding.scrollViewCori.visibility = View.GONE
            }else if(binding.checkBoxCori.isChecked){
                binding.imageButtonCori.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_drop_down_48, null))
                binding.scrollViewCori.visibility = View.VISIBLE
                adattaView(binding.scrollViewCori)
            }
        }



    }

    private fun clearCheckChitarra(linearLayout: LinearLayout){
        for (i in 0 until linearLayout.childCount) {
            val child = linearLayout.getChildAt(i)
            if (child is CheckBox) {
                child.isChecked = false // Rimuovi il check dal CheckBox
            }
        }
    }

    private fun adattaView(view: NestedScrollView){
        val altezza = calculateScrollViewHeight(view)
        // Imposta l'altezza massima
        if (altezza > dpToPx() || altezza == 0) {
            val maxHeight = dpToPx()
            val params = view.layoutParams
            params.height = maxHeight
            view.layoutParams = params
        }

        val larghezzaMassima = if(resources.configuration.smallestScreenWidthDp >= DIMENSIONE_TABLET_MINIMA){
            MAX_WIDTH_TABLET
        }else{
            MAX_WIDTH
        }

        val larghezza = calcolaWidthMax(view)
        if(larghezza > dpToPx(larghezzaMassima) || larghezza == 0){
            val maxWidth = dpToPx(larghezzaMassima)
            val params = view.layoutParams
            params.width = maxWidth
            view.layoutParams = params
        }
    }

    private fun calcolaWidthMax(batteria: NestedScrollView): Int {
        var maxWidth = 0
        for (i in 0 until batteria.childCount) {
            val child = batteria.getChildAt(i)
            if(maxWidth < child.width){
                maxWidth = child.width
            }
        }
        return maxWidth
    }

    /**
     * Funzione per convertire dp in pixel
     */
    private fun dpToPx(dp: Int = MAX_HEIGHT): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

   /**
    * Funzione per calcolare l'altezza del LinearLayout
    * */
    private fun calculateScrollViewHeight(nestedScrollView: NestedScrollView): Int {
        var totalHeight = 0
        for (i in 0 until nestedScrollView.childCount) {
            val child = nestedScrollView.getChildAt(i)
            totalHeight += child.measuredHeight
        }
        return totalHeight
    }

}