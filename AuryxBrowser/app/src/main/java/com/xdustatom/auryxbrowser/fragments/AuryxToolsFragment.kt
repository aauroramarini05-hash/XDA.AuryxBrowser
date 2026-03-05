package com.xdustatom.auryxbrowser.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.xdustatom.auryxbrowser.R
import com.xdustatom.auryxbrowser.activities.MainActivity

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

        // ✅ PageInfo (usa newInstance, così non chiede parametri nel costruttore)
        btnPageInfo.setOnClickListener { openTool(PageInfoFragment.newInstance()) }

        btnDownloads.setOnClickListener { openTool(DownloadsFragment()) }

        // ✅ FIX: AssistantFragment richiede onAction
        btnAssistant.setOnClickListener {
            openTool(
                AssistantFragment { action, data ->
                    val act = activity
                    if (act is MainActivity) {
                        act.performAssistantAction(action, data)
                    } else {
                        Toast.makeText(requireContext(), "Action not available", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    private fun openTool(fragment: Fragment) {
        try {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        } catch (_: Throwable) {
            Toast.makeText(requireContext(), "Can't open tool", Toast.LENGTH_SHORT).show()
        }
    }
}
