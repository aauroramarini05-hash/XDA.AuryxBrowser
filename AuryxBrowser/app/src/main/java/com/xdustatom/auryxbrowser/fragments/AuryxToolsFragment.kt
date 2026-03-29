package com.xdustatom.auryxbrowser.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.xdustatom.auryxbrowser.R
import com.xdustatom.auryxbrowser.activities.MainActivity
import com.xdustatom.auryxbrowser.remote.RemoteConfigRepository
import com.xdustatom.auryxbrowser.ui.animateEntrance

class AuryxToolsFragment : Fragment(R.layout.fragment_tools) {

    companion object {
        fun newInstance(): AuryxToolsFragment = AuryxToolsFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val assistantButton = view.findViewById<View>(R.id.btnAssistant)

        val remoteConfig = RemoteConfigRepository(requireContext()).cached()
        val assistantEnabled = remoteConfig.flags["assistant_enabled"] ?: false
        assistantButton.visibility = if (assistantEnabled) View.VISIBLE else View.GONE

        val cards = listOf(
            view.findViewById<View>(R.id.btnNetworkMonitor),
            view.findViewById<View>(R.id.btnDeviceInfo),
            view.findViewById<View>(R.id.btnPerformance),
            view.findViewById<View>(R.id.btnPageInfo),
            view.findViewById<View>(R.id.btnDownloads),
            assistantButton
        ).filter { it.visibility == View.VISIBLE }

        cards.forEachIndexed { index, card ->
            card.animateEntrance(delay = index * 42L)
        }

        view.findViewById<View>(R.id.btnDeviceInfo).setOnClickListener { openTool(DeviceInfoFragment()) }
        view.findViewById<View>(R.id.btnNetworkMonitor).setOnClickListener { openTool(NetworkMonitorFragment()) }
        view.findViewById<View>(R.id.btnPerformance).setOnClickListener { openTool(PerformanceFragment()) }
        view.findViewById<View>(R.id.btnPageInfo).setOnClickListener {
            val host = activity
            if (host is MainActivity) {
                openTool(host.buildPageInfoFragment())
            } else {
                openTool(PageInfoFragment.newInstance())
            }
        }
        view.findViewById<View>(R.id.btnDownloads).setOnClickListener { openTool(DownloadsFragment()) }

        assistantButton.setOnClickListener {
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
        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}
