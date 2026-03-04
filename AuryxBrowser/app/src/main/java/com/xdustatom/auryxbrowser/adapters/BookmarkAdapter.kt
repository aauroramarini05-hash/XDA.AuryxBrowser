package com.xdustatom.auryxbrowser.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.xdustatom.auryxbrowser.databinding.ItemBookmarkBinding
import com.xdustatom.auryxbrowser.models.Bookmark

class BookmarkAdapter(
    private val bookmarks: List<Bookmark>,
    private val onItemClick: (Bookmark) -> Unit,
    private val onDeleteClick: (Bookmark) -> Unit
) : RecyclerView.Adapter<BookmarkAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemBookmarkBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bookmark: Bookmark) {
            binding.tvTitle.text = bookmark.title
            binding.tvUrl.text = bookmark.url
            binding.tvIcon.text = bookmark.title.firstOrNull()?.uppercase() ?: "B"
            
            binding.root.setOnClickListener { onItemClick(bookmark) }
            binding.btnDelete.setOnClickListener { onDeleteClick(bookmark) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookmarkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(bookmarks[position])
    }

    override fun getItemCount() = bookmarks.size
}
