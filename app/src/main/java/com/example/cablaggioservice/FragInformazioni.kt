package com.example.cablaggioservice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.cablaggioservice.databinding.FragInformazioniBinding
import com.example.cablaggioservice.databinding.FragSezioniBinding

class FragInformazioni: Fragment(R.layout.frag_informazioni) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private lateinit var binding: FragInformazioniBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragInformazioniBinding.inflate(inflater, container, false)
        return binding.root
    }

}