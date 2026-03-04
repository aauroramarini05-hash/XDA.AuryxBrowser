package com.xdustatom.auryxbrowser.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.xdustatom.auryxbrowser.databinding.ItemTabBinding
import com.xdustatom.auryxbrowser.models.Tab

class TabsAdapter(
    private val tabs: List<Tab>,
    private val onTabClick: (Int) -> Unit,
    private val onCloseClick: (Int) -> Unit
) : RecyclerView.Adapter<TabsAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemTabBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tab: Tab, position: Int) {
            binding.tvTitle.text = if (tab.title.isNotEmpty()) tab.title else "New Tab"
            binding.tvUrl.text = if (tab.url.isNotEmpty()) tab.url else "about:blank"
            binding.tvIcon.text = tab.title.firstOrNull()?.uppercase() ?: "T"
            
            // Show favicon if available
            if (tab.favicon != null) {
                binding.imgFavicon.setImageBitmap(tab.favicon)
                binding.imgFavicon.isVisible = true
                binding.tvIcon.isVisible = false
            } else {
                binding.imgFavicon.isVisible = false
                binding.tvIcon.isVisible = true
            }
            
            binding.root.setOnClickListener { onTabClick(position) }
            binding.btnClose.setOnClickListener { onCloseClick(position) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTabBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tabs[position], position)
    }

    override fun getItemCount() = tabs.size
}
