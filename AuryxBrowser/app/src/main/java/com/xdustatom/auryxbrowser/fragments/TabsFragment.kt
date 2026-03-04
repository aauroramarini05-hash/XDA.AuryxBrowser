package com.xdustatom.auryxbrowser.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.xdustatom.auryxbrowser.adapters.TabsAdapter
import com.xdustatom.auryxbrowser.databinding.FragmentTabsBinding
import com.xdustatom.auryxbrowser.models.Tab

class TabsFragment(
    private val tabs: List<Tab>,
    private val onTabSelected: (Int) -> Unit,
    private val onTabClosed: (Int) -> Unit,
    private val onNewTab: () -> Unit
) : Fragment() {

    private var _binding: FragmentTabsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTabsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.tvTabCount.text = "${tabs.size} tabs"
        
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = TabsAdapter(
            tabs = tabs,
            onTabClick = { index ->
                onTabSelected(index)
                parentFragmentManager.popBackStack()
            },
            onCloseClick = { index ->
                onTabClosed(index)
                if (tabs.size <= 1) {
                    parentFragmentManager.popBackStack()
                }
            }
        )
        
        binding.fabNewTab.setOnClickListener {
            onNewTab()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
