package com.xdustatom.auryxbrowser.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.xdustatom.auryxbrowser.AuryxApplication
import com.xdustatom.auryxbrowser.R
import com.xdustatom.auryxbrowser.adapters.HistoryAdapter
import com.xdustatom.auryxbrowser.databinding.FragmentHistoryBinding

class HistoryFragment(private val onHistoryClick: (String) -> Unit) : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val prefs by lazy { AuryxApplication.instance.preferencesManager }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        
        binding.btnClearHistory.setOnClickListener {
            AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle("Clear History")
                .setMessage("Are you sure you want to clear all browsing history?")
                .setPositiveButton("Clear") { _, _ ->
                    prefs.clearHistory()
                    setupRecyclerView()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setupRecyclerView() {
        val history = prefs.getHistory()
        
        if (history.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
            binding.btnClearHistory.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            binding.btnClearHistory.visibility = View.VISIBLE
            
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.adapter = HistoryAdapter(
                items = history,
                onItemClick = { item -> onHistoryClick(item.url) }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
