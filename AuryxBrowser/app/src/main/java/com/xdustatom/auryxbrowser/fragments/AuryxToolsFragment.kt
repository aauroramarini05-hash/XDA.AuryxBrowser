package com.xdustatom.auryxbrowser.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.xdustatom.auryxbrowser.activities.MainActivity
import com.xdustatom.auryxbrowser.databinding.FragmentAuryxToolsBinding

class AuryxToolsFragment : Fragment() {

    private var _binding: FragmentAuryxToolsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAuryxToolsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.cardNetworkMonitor.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.xdustatom.auryxbrowser.R.id.fragmentContainer, NetworkMonitorFragment())
                .addToBackStack(null)
                .commit()
        }
        
        binding.cardDeviceInfo.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.xdustatom.auryxbrowser.R.id.fragmentContainer, DeviceInfoFragment())
                .addToBackStack(null)
                .commit()
        }
        
        binding.cardPerformance.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.xdustatom.auryxbrowser.R.id.fragmentContainer, PerformanceFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.cardPageInfo.setOnClickListener {
            // Navigate to Page Info through MainActivity
            (activity as? MainActivity)?.let { mainActivity ->
                // Trigger page info from menu
                parentFragmentManager.popBackStack()
            }
        }

        binding.cardAssistant.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.xdustatom.auryxbrowser.R.id.fragmentContainer, AssistantFragment { action, data ->
                    (activity as? MainActivity)?.performAssistantAction(action, data)
                })
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
