package com.xdustatom.auryxbrowser.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.xdustatom.auryxbrowser.databinding.FragmentNetworkMonitorBinding
import com.xdustatom.auryxbrowser.utils.NetworkUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NetworkMonitorFragment : Fragment() {

    private var _binding: FragmentNetworkMonitorBinding? = null
    private val binding get() = _binding!!
    private var updateJob: Job? = null

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
                delay(2500)
            }
        }
    }

    private fun updateNetworkInfo() {
        val context = context ?: return

        binding.tvConnectionType.text = NetworkUtils.getConnectionType(context)
        binding.tvNetworkState.text = NetworkUtils.getNetworkState(context)

        viewLifecycleOwner.lifecycleScope.launch {
            val ip = NetworkUtils.getPublicIp()
            _binding?.tvPublicIp?.text = ip
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val latency = NetworkUtils.measureLatency()
            _binding?.tvLatency?.text = if (latency >= 0) "${latency}ms" else "N/A"
        }
    }

    private fun runPrecisionTest() {
        binding.btnRunNetworkTest.isEnabled = false
        binding.btnRunNetworkTest.text = "Testing..."

        viewLifecycleOwner.lifecycleScope.launch {
            val report = NetworkUtils.runConnectivityTest()
            _binding?.apply {
                tvNetworkState.text = report.state
                tvLatency.text = if (report.latencyMs >= 0) "${report.latencyMs}ms" else "N/A"
                tvJitter.text = if (report.jitterMs >= 0) "${report.jitterMs}ms" else "N/A"
                tvPacketLoss.text = "${report.packetLossPercent}%"
                tvDownloadSpeed.text =
                    if (report.downloadMbps > 0) String.format("%.2f Mbps", report.downloadMbps)
                    else "N/A"
                btnRunNetworkTest.isEnabled = true
                btnRunNetworkTest.text = "Run Precision Test"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updateJob?.cancel()
        _binding = null
    }
}
