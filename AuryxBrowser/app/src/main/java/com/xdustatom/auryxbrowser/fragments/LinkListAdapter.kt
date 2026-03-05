package com.xdustatom.auryxbrowser.fragments

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.xdustatom.auryxbrowser.R

class LinkListAdapter(
    ctx: Context,
    private val items: List<String>,
    private val iconText: String
) : ArrayAdapter<String>(ctx, 0, items) {

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): String = items[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_link, parent, false)

        val url = getItem(position)
        val host = extractHost(url)

        val tvIcon = v.findViewById<TextView>(R.id.tvIcon)
        val tvTitle = v.findViewById<TextView>(R.id.tvTitle)
        val tvSubtitle = v.findViewById<TextView>(R.id.tvSubtitle)

        tvIcon.text = iconText
        tvTitle.text = if (host.isNotBlank()) host else "Link"
        tvSubtitle.text = url

        return v
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
}
