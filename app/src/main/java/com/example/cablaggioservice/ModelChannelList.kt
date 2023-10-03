package com.example.cablaggioservice

/**
 * @param id numero della lista
 * @param nomeGruppo il nome del gruppo di strumenti (es. BATTERIA)
 * @param strumento nome strumento (es. timpano, kik)
 * @param type si divide in MIC e DI
 * @param frusta1 prima frusta contente tot canali
 * @param micAste contiene il nome dei microfoni o aste
 * @param aux contiene i tipi di aux numerati
 */
class ModelChannelList(val id: String,val nomeGruppo: String = "",val strumento: String,val type: String,
                       val frusta1: String, val frusta2: String,
                       val frusta3: String,val  frusta4: String,
                       val micAste: String,val  aux: String
) {

    // Metodo per ottenere un iteratore sui valori del modello
    operator fun iterator(): Iterator<String> {
        return listOf(
            id,
            nomeGruppo,
            strumento,
            type,
            frusta1,
            frusta2,
            frusta3,
            frusta4,
            micAste,
            aux
        ).iterator()
    }

    override fun toString(): String {
        return "ModelChannelList(id='$id', nomeGruppo='$nomeGruppo', strumento='$strumento', " +
                "type='$type', frusta1='$frusta1', frusta2='$frusta2', frusta3='$frusta3', " +
                "frusta4='$frusta4', micAste='$micAste', aux='$aux')"
    }



}