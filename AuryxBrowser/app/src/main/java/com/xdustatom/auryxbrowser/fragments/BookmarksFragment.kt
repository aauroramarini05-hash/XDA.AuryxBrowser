package com.xdustatom.auryxbrowser.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.xdustatom.auryxbrowser.R
import com.xdustatom.auryxbrowser.activities.BrowserStore

class BookmarksFragment : Fragment(R.layout.fragment_bookmarks) {

    companion object {
        fun newInstance(): BookmarksFragment = BookmarksFragment()
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
            val url = list.adapter.getItem(position) as String
            openUrl(url)
        }

        list.setOnItemLongClickListener { _, _, position, _ ->
            val url = list.adapter.getItem(position) as String
            store.removeBookmark(url)
            Toast.makeText(requireContext(), "Bookmark removed", Toast.LENGTH_SHORT).show()
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

        val adapter = object : ArrayAdapter<String>(requireContext(), 0, data) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.list_item_link, parent, false)

                val url = getItem(position).orEmpty()
                val host = extractHost(url)

                // ✅ Icona vera (no emoji)
                v.findViewById<ImageView>(R.id.tvIcon).setImageResource(R.drawable.ic_bookmark)

                v.findViewById<TextView>(R.id.tvTitle).text =
                    if (host.isNotBlank()) host else "Bookmark"

                v.findViewById<TextView>(R.id.tvSubtitle).text = url

                return v
            }
        }

        list.adapter = adapter

        if (data.isEmpty()) {
            Toast.makeText(requireContext(), "No bookmarks yet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun extractHost(url: String): String {
        return try {
            val uri = Uri.parse(url)
            val h = uri.host ?: ""
            if (h.startsWith("www.")) h.substring(4) else h
        } catch (_: Throwable) {
            ""
        }
    }

    private fun openUrl(url: String) {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }.onFailure {
            Toast.makeText(requireContext(), "Cannot open URL", Toast.LENGTH_SHORT).show()
        }
    }
}
