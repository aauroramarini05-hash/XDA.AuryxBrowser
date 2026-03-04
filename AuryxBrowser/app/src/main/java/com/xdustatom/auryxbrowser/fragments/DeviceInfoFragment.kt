package com.xdustatom.auryxbrowser.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.xdustatom.auryxbrowser.databinding.FragmentDeviceInfoBinding
import com.xdustatom.auryxbrowser.utils.DeviceUtils

class DeviceInfoFragment : Fragment() {

    private var _binding: FragmentDeviceInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDeviceInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayDeviceInfo()
    }

    private fun displayDeviceInfo() {
        val context = context ?: return
        
        binding.tvDeviceModel.text = DeviceUtils.getDeviceModel()
        binding.tvManufacturer.text = DeviceUtils.getManufacturer()
        binding.tvCpuArchitecture.text = DeviceUtils.getCpuArchitecture()
        binding.tvTotalRam.text = DeviceUtils.getTotalRam(context)
        binding.tvAndroidVersion.text = DeviceUtils.getAndroidVersion()
        binding.tvBatteryLevel.text = "${DeviceUtils.getBatteryLevel(context)}%"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
