package com.example.cablaggioservice

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class CustomEditText : AppCompatEditText {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun performClick(): Boolean {
        // Implementa qui le azioni desiderate quando viene effettuato un clic
        return super.performClick()
    }


}
