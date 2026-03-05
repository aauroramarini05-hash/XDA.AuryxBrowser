package com.xdustatom.auryxbrowser.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.xdustatom.auryxbrowser.R
import com.xdustatom.auryxbrowser.activities.BrowserStore

class HistoryFragment : Fragment(R.layout.fragment_history) {

    companion object {
        fun newInstance() = HistoryFragment()
    }

    private lateinit var store: BrowserStore
    private lateinit var list: ListView
    private lateinit var btnClear: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        store = BrowserStore(requireContext())
        list = view.findViewById(R.id.historyList)
        btnClear = view.findViewById(R.id.btnClearHistory)

        render()

        list.setOnItemClickListener { _, _, position, _ ->
            val url = (list.adapter.getItem(position) as String)
            openUrl(url)
        }

        btnClear.setOnClickListener {
            store.clearHistory()
            Toast.makeText(requireContext(), "History cleared", Toast.LENGTH_SHORT).show()
            render()
        }
    }

    private fun render() {
        val data = store.getHistory()
        list.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, data)
        if (data.isEmpty()) {
            Toast.makeText(requireContext(), "History is empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openUrl(url: String) {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }.onFailure {
            Toast.makeText(requireContext(), "Can't open", Toast.LENGTH_SHORT).show()
        }
    }
}
