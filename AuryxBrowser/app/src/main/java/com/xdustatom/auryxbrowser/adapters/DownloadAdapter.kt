package com.xdustatom.auryxbrowser.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.xdustatom.auryxbrowser.databinding.ItemDownloadBinding
import com.xdustatom.auryxbrowser.models.DownloadItem
import com.xdustatom.auryxbrowser.models.DownloadStatus

class DownloadAdapter(
    private val items: List<DownloadItem>,
    private val onItemClick: (DownloadItem) -> Unit
) : RecyclerView.Adapter<DownloadAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemDownloadBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DownloadItem) {
            binding.tvFileName.text = item.fileName
            binding.tvStatus.text = when (item.status) {
                DownloadStatus.PENDING -> "Pending"
                DownloadStatus.DOWNLOADING -> "Downloading..."
                DownloadStatus.COMPLETED -> "Completed"
                DownloadStatus.FAILED -> "Failed"
                DownloadStatus.CANCELLED -> "Cancelled"
            }
            
            binding.progressBar.isVisible = item.status == DownloadStatus.DOWNLOADING
            binding.progressBar.progress = item.progress
            
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDownloadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
