package com.xdustatom.auryxbrowser.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.xdustatom.auryxbrowser.AuryxApplication
import com.xdustatom.auryxbrowser.adapters.BookmarkAdapter
import com.xdustatom.auryxbrowser.databinding.FragmentBookmarksBinding

class BookmarksFragment(private val onBookmarkClick: (String) -> Unit) : Fragment() {

    private var _binding: FragmentBookmarksBinding? = null
    private val binding get() = _binding!!
    private val prefs by lazy { AuryxApplication.instance.preferencesManager }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookmarksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val bookmarks = prefs.getBookmarks()
        
        if (bookmarks.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.adapter = BookmarkAdapter(
                bookmarks = bookmarks,
                onItemClick = { bookmark -> onBookmarkClick(bookmark.url) },
                onDeleteClick = { bookmark ->
                    prefs.removeBookmark(bookmark.id)
                    setupRecyclerView()
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
