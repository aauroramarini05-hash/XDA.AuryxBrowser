package com.xdustatom.auryxbrowser.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.xdustatom.auryxbrowser.R
import com.xdustatom.auryxbrowser.playservices.GoogleServices

class PlayServicesFragment : Fragment(R.layout.fragment_play_services) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val manager = GoogleServices.get()
        val tvStatus = view.findViewById<TextView>(R.id.tvPlayStatus)

        val available = manager?.isPlayServicesAvailable() == true
        tvStatus.text = if (available) "Available" else "Unavailable"

        view.findViewById<MaterialButton>(R.id.btnCheckUpdatesPlay).setOnClickListener {
            val ok = manager?.startImmediateUpdateIfAvailable(requireActivity()) == true
            if (!ok) {
                Toast.makeText(requireContext(), "No Play update available", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<MaterialButton>(R.id.btnRateApp).setOnClickListener {
            val launched = manager?.requestInAppReview(requireActivity()) == true
            if (!launched) {
                Toast.makeText(requireContext(), "Play review unavailable", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<MaterialButton>(R.id.btnRepairPlay).setOnClickListener {
            val shown = manager?.showResolvableErrorDialog(requireActivity()) == true
            if (!shown) {
                Toast.makeText(requireContext(), "Play Services already healthy", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
