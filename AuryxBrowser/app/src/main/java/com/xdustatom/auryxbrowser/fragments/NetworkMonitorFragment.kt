package com.xdustatom.auryxbrowser.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.xdustatom.auryxbrowser.databinding.FragmentNetworkMonitorBinding
import com.xdustatom.auryxbrowser.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NetworkMonitorFragment : Fragment() {

    private var _binding: FragmentNetworkMonitorBinding? = null
    private val binding get() = _binding!!
    private var updateJob: Job? = null
    private var precisionJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNetworkMonitorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnRunNetworkTest.setOnClickListener { runPrecisionTest() }
        startMonitoring()
    }

    private fun startMonitoring() {
        updateJob?.cancel()
        updateJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                updateNetworkInfo()
                delay(3000)
            }
        }
    }

    private fun updateNetworkInfo() {
        val context = context ?: return
        val currentBinding = _binding ?: return

        currentBinding.tvConnectionType.text = runCatching {
            NetworkUtils.getConnectionType(context)
        }.getOrDefault("Unknown")

        currentBinding.tvNetworkState.text = runCatching {
            NetworkUtils.getNetworkState(context)
        }.getOrDefault("Unknown")

        val (ip, latency) = withContext(Dispatchers.IO) {
            val safeIp = try {
                NetworkUtils.getPublicIp()
            } catch (_: Exception) {
                "Unavailable"
            }
            val safeLatency = try {
                NetworkUtils.measureLatency()
            } catch (_: Exception) {
                -1L
            }
            safeIp to safeLatency
        }

        _binding?.apply {
            tvPublicIp.text = ip
            tvLatency.text = if (latency >= 0) "${latency}ms" else "N/A"
        }
    }

    private fun runPrecisionTest() {
        precisionJob?.cancel()
        binding.btnRunNetworkTest.isEnabled = false
        binding.btnRunNetworkTest.text = "Testing..."

        precisionJob = viewLifecycleOwner.lifecycleScope.launch {
            val report = withContext(Dispatchers.IO) {
                try {
                    NetworkUtils.runConnectivityTest()
                } catch (_: Exception) {
                    null
                }
            }

            _binding?.apply {
                if (report == null) {
                    tvNetworkState.text = "Unavailable"
                    tvJitter.text = "N/A"
                    tvPacketLoss.text = "N/A"
                    tvDownloadSpeed.text = "N/A"
                } else {
                    tvNetworkState.text = report.state
                    tvLatency.text = if (report.latencyMs >= 0) "${report.latencyMs}ms" else "N/A"
                    tvJitter.text = if (report.jitterMs >= 0) "${report.jitterMs}ms" else "N/A"
                    tvPacketLoss.text = "${report.packetLossPercent}%"
                    tvDownloadSpeed.text =
                        if (report.downloadMbps > 0) String.format("%.2f Mbps", report.downloadMbps)
                        else "N/A"
                }
                btnRunNetworkTest.isEnabled = true
                btnRunNetworkTest.text = "Run Precision Test"
            }
        }
    }

    override fun onDestroyView() {
        updateJob?.cancel()
        precisionJob?.cancel()
        _binding = null
        super.onDestroyView()
    }
}
