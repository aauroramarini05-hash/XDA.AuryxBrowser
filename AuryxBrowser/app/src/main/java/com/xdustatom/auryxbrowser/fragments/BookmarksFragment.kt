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

class BookmarksFragment : Fragment(R.layout.fragment_bookmarks) {

    companion object {
        fun newInstance() = BookmarksFragment()
    }

    private lateinit var store: BrowserStore
    private lateinit var list: ListView
    private lateinit var btnClear: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        store = BrowserStore(requireContext())
        list = view.findViewById(R.id.bookmarksList)
        btnClear = view.findViewById(R.id.btnClearBookmarks)

        render()

        list.setOnItemClickListener { _, _, position, _ ->
            val url = (list.adapter.getItem(position) as String)
            openUrl(url)
        }

        list.setOnItemLongClickListener { _, _, position, _ ->
            val url = (list.adapter.getItem(position) as String)
            store.removeBookmark(url)
            Toast.makeText(requireContext(), "Removed bookmark", Toast.LENGTH_SHORT).show()
            render()
            true
        }

        btnClear.setOnClickListener {
            store.clearBookmarks()
            Toast.makeText(requireContext(), "Bookmarks cleared", Toast.LENGTH_SHORT).show()
            render()
        }
    }

    private fun render() {
        val data = store.getBookmarks()
        list.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, data)
        if (data.isEmpty()) {
            Toast.makeText(requireContext(), "No bookmarks yet (use Menu → Add bookmark)", Toast.LENGTH_SHORT).show()
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
