package com.example.cablaggioservice


import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment


class FragContatti: Fragment(R.layout.contatti) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textViewGitHubProfile: TextView = view.findViewById(R.id.textViewGitHubProfile)
        textViewGitHubProfile.movementMethod = LinkMovementMethod.getInstance()

    }
}