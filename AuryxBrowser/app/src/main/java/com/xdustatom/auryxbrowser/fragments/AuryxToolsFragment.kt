package com.xdustatom.auryxbrowser.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.xdustatom.auryxbrowser.R

class AuryxToolsFragment : Fragment(R.layout.fragment_tools) {

    companion object {
        fun newInstance(): AuryxToolsFragment {
            return AuryxToolsFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Qui puoi aggiungere strumenti futuri:
        // network monitor
        // device info
        // performance monitor
        // page info
    }
}
