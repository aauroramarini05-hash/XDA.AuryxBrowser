package com.xdustatom.auryxbrowser.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.xdustatom.auryxbrowser.databinding.FragmentPerformanceBinding
import com.xdustatom.auryxbrowser.utils.DeviceUtils
import kotlinx.coroutines.*

class PerformanceFragment : Fragment() {

    private var _binding: FragmentPerformanceBinding? = null
    private val binding get() = _binding!!
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var updateJob: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPerformanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startMonitoring()
    }

    private fun startMonitoring() {
        updateJob = scope.launch {
            while (isActive) {
                updatePerformanceInfo()
                delay(1000)
            }
        }
    }

    private fun updatePerformanceInfo() {
        val context = context ?: return
        
        val ramUsage = DeviceUtils.getRamUsagePercent(context)
        val usedRam = DeviceUtils.getUsedRam(context)
        val availableRam = DeviceUtils.getAvailableRam(context)
        val cpuUsage = DeviceUtils.getCpuUsage()
        
        binding.tvRamUsage.text = "$usedRam MB used"
        binding.tvRamAvailable.text = "$availableRam MB available"
        binding.progressRam.progress = ramUsage
        binding.tvRamPercent.text = "$ramUsage%"
        
        binding.tvCpuUsage.text = "${cpuUsage.toInt()}%"
        binding.progressCpu.progress = cpuUsage.toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updateJob?.cancel()
        scope.cancel()
        _binding = null
    }
}
