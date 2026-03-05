package com.xdustatom.auryxbrowser.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.xdustatom.auryxbrowser.R

class AuryxToolsFragment : Fragment(R.layout.fragment_tools) {

    companion object {
        fun newInstance(): AuryxToolsFragment = AuryxToolsFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnDeviceInfo = view.findViewById<Button>(R.id.btnDeviceInfo)
        val btnNetworkMonitor = view.findViewById<Button>(R.id.btnNetworkMonitor)
        val btnPerformance = view.findViewById<Button>(R.id.btnPerformance)
        val btnPageInfo = view.findViewById<Button>(R.id.btnPageInfo)
        val btnDownloads = view.findViewById<Button>(R.id.btnDownloads)
        val btnAssistant = view.findViewById<Button>(R.id.btnAssistant)

        btnDeviceInfo.setOnClickListener { openTool(DeviceInfoFragment()) }
        btnNetworkMonitor.setOnClickListener { openTool(NetworkMonitorFragment()) }
        btnPerformance.setOnClickListener { openTool(PerformanceFragment()) }
        btnPageInfo.setOnClickListener { openTool(PageInfoFragment()) }
        btnDownloads.setOnClickListener { openTool(DownloadsFragment()) }
        btnAssistant.setOnClickListener { openTool(AssistantFragment()) }
    }

    private fun openTool(fragment: Fragment) {
        try {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        } catch (e: Throwable) {
            Toast.makeText(requireContext(), "Can't open tool", Toast.LENGTH_SHORT).show()
        }
    }
}
