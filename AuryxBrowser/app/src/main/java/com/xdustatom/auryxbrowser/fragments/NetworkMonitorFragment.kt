package com.xdustatom.auryxbrowser.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.xdustatom.auryxbrowser.databinding.FragmentNetworkMonitorBinding
import com.xdustatom.auryxbrowser.utils.NetworkUtils
import kotlinx.coroutines.*

class NetworkMonitorFragment : Fragment() {

    private var _binding: FragmentNetworkMonitorBinding? = null
    private val binding get() = _binding!!
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var updateJob: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNetworkMonitorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startMonitoring()
    }

    private fun startMonitoring() {
        updateJob = scope.launch {
            while (isActive) {
                updateNetworkInfo()
                delay(2000)
            }
        }
    }

    private suspend fun updateNetworkInfo() {
        val context = context ?: return
        
        binding.tvConnectionType.text = NetworkUtils.getConnectionType(context)
        binding.tvNetworkState.text = NetworkUtils.getNetworkState(context)
        
        // Get public IP (requires network)
        scope.launch(Dispatchers.IO) {
            val ip = try {
                NetworkUtils.getPublicIp()
            } catch (e: Exception) {
                "Offline"
            }
            withContext(Dispatchers.Main) {
                _binding?.tvPublicIp?.text = ip
            }
        }
        
        // Measure latency
        scope.launch(Dispatchers.IO) {
            val latency = try {
                NetworkUtils.measureLatency()
            } catch (e: Exception) {
                -1L
            }
            withContext(Dispatchers.Main) {
                _binding?.tvLatency?.text = if (latency >= 0) "${latency}ms" else "N/A"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updateJob?.cancel()
        scope.cancel()
        _binding = null
    }
}
