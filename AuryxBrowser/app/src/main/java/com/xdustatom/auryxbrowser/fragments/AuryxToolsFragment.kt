package com.xdustatom.auryxbrowser.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.xdustatom.auryxbrowser.R
import com.xdustatom.auryxbrowser.activities.MainActivity
import com.xdustatom.auryxbrowser.databinding.FragmentAuryxToolsBinding

class AuryxToolsFragment : Fragment() {

    private var _binding: FragmentAuryxToolsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuryxToolsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardNetworkMonitor.setOnClickListener {
            openFragment(NetworkMonitorFragment())
        }

        binding.cardDeviceInfo.setOnClickListener {
            openFragment(DeviceInfoFragment())
        }

        binding.cardPerformance.setOnClickListener {
            openFragment(PerformanceFragment())
        }

        binding.cardPageInfo.setOnClickListener {
            // Qui puoi implementare davvero una schermata "Page Info" se vuoi.
            // Per ora: torna indietro (comportamento originale, ma senza variabile inutilizzata).
            parentFragmentManager.popBackStack()
        }

        binding.cardAssistant.setOnClickListener {
            openFragment(
                AssistantFragment { action, data ->
                    (activity as? MainActivity)?.performAssistantAction(action, data)
                }
            )
        }
    }

    private fun openFragment(fragment: Fragment) {
        // Se non hai un container con questo id, evitiamo crash.
        val containerId = R.id.fragmentContainer
        if (containerId == 0) return

        parentFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
