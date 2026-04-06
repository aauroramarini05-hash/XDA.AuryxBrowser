package com.xdustatom.auryxbrowser.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.button.MaterialButton
import com.xdustatom.auryxbrowser.R
import com.xdustatom.auryxbrowser.activities.MainActivity

class LocalPulseFragment : Fragment(R.layout.fragment_local_pulse) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.any { it }) {
            detectLocation()
        } else {
            Toast.makeText(requireContext(), "Permesso posizione negato", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        view.findViewById<MaterialButton>(R.id.btnDetectLocation).setOnClickListener {
            detectLocation()
        }

        view.findViewById<MaterialButton>(R.id.btnSearchNearby).setOnClickListener {
            openQuery("ristoranti vicino a me")
        }
        view.findViewById<MaterialButton>(R.id.btnFuel).setOnClickListener {
            openQuery("benzinaio vicino a me")
        }
        view.findViewById<MaterialButton>(R.id.btnPharmacy).setOnClickListener {
            openQuery("farmacia vicino a me")
        }
    }

    private fun detectLocation() {
        if (!hasLocationPermission()) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        val token = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, token.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    lastLocation = location
                    view?.findViewById<TextView>(R.id.tvLocationStatus)?.text =
                        "Posizione rilevata: %.4f, %.4f".format(location.latitude, location.longitude)
                } else {
                    view?.findViewById<TextView>(R.id.tvLocationStatus)?.text = "Posizione non disponibile"
                }
            }
            .addOnFailureListener {
                view?.findViewById<TextView>(R.id.tvLocationStatus)?.text = "Errore GPS"
            }
    }

    private fun openQuery(query: String) {
        val location = lastLocation
        val enrichedQuery = if (location != null) {
            "$query ${location.latitude},${location.longitude}"
        } else {
            query
        }
        (activity as? MainActivity)?.performAssistantAction("search", enrichedQuery)
        parentFragmentManager.popBackStack()
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }
}
