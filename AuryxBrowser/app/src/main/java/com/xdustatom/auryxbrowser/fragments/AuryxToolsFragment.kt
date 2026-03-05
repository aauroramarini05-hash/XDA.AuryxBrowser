package com.xdustatom.auryxbrowser.fragments

import android.os.Bundle
import android.view.View
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

        view.findViewById<View>(R.id.btnDeviceInfo).setOnClickListener { openTool(DeviceInfoFragment()) }
        view.findViewById<View>(R.id.btnNetworkMonitor).setOnClickListener { openTool(NetworkMonitorFragment()) }
        view.findViewById<View>(R.id.btnPerformance).setOnClickListener { openTool(PerformanceFragment()) }
        view.findViewById<View>(R.id.btnPageInfo).setOnClickListener { openTool(PageInfoFragment.newInstance()) }

        // Downloads ID esiste ma è nascosto (se vuoi lo rendiamo card anche lui)
        view.findViewById<View>(R.id.btnDownloads)?.setOnClickListener { openTool(DownloadsFragment()) }

        view.findViewById<View>(R.id.btnAssistant).setOnClickListener {
            openTool(
                AssistantFragment { action, data ->
                    val act = activity
                    if (act is MainActivity) act.performAssistantAction(action, data)
                    else Toast.makeText(requireContext(), "Action not available", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun openTool(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}
