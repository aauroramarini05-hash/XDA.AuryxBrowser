package com.xdustatom.auryxbrowser.tabs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xdustatom.auryxbrowser.R

class TabsAdapter(
    private val items: List<BrowserTab>,
    private val selectedTabId: Long,
    private val onTabClick: (BrowserTab) -> Unit,
    private val onTabClose: (BrowserTab) -> Unit
) : RecyclerView.Adapter<TabsAdapter.TabViewHolder>() {

    class TabViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tabTitle)
        val subtitle: TextView = view.findViewById(R.id.tabSubtitle)
        val close: ImageButton = view.findViewById(R.id.tabClose)
        val selectedIndicator: View = view.findViewById(R.id.tabSelectedIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_tab, parent, false)
        return TabViewHolder(view)
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title.ifBlank { "New Tab" }
        holder.subtitle.text = item.url.ifBlank { "Empty page" }
        holder.selectedIndicator.visibility =
            if (item.id == selectedTabId) View.VISIBLE else View.INVISIBLE

        holder.itemView.setOnClickListener { onTabClick(item) }
        holder.close.setOnClickListener { onTabClose(item) }
    }

    override fun getItemCount(): Int = items.size
}
