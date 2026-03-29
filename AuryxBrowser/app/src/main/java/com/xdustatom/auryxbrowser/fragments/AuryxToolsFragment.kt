package com.xdustatom.auryxbrowser.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.xdustatom.auryxbrowser.R
import com.xdustatom.auryxbrowser.activities.MainActivity
import com.xdustatom.auryxbrowser.ui.animateEntrance

class AuryxToolsFragment : Fragment(R.layout.fragment_tools) {

    companion object {
        fun newInstance(): AuryxToolsFragment = AuryxToolsFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val assistantButton = view.findViewById<View>(R.id.btnAssistant)
        assistantButton.visibility = View.GONE

        val cards = listOf(
            view.findViewById<View>(R.id.btnNetworkMonitor),
            view.findViewById<View>(R.id.btnDeviceInfo),
            view.findViewById<View>(R.id.btnPerformance),
            view.findViewById<View>(R.id.btnPageInfo),
            view.findViewById<View>(R.id.btnDownloads)
        )

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
