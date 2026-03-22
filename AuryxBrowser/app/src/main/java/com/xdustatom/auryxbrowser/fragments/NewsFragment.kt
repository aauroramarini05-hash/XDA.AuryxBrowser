package com.xdustatom.auryxbrowser.fragments

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.xdustatom.auryxbrowser.R
import com.xdustatom.auryxbrowser.activities.MainActivity
import com.xdustatom.auryxbrowser.news.NewsAdapter
import com.xdustatom.auryxbrowser.news.NewsFeedResponse
import com.xdustatom.auryxbrowser.news.NewsRepository
import kotlin.concurrent.thread

class NewsFragment : Fragment(R.layout.fragment_news) {

    companion object {
        fun newInstance(): NewsFragment = NewsFragment()
    }

    private lateinit var repository: NewsRepository
    private lateinit var adapter: NewsAdapter

    private var fullFeed: NewsFeedResponse? = null
    private var selectedCountry: String = "all"
    private var selectedCategory: String = "all"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = NewsRepository(requireContext())

        val dropCountry = view.findViewById<MaterialAutoCompleteTextView>(R.id.dropCountry)
        val dropCategory = view.findViewById<MaterialAutoCompleteTextView>(R.id.dropCategory)
        val btnRefresh = view.findViewById<MaterialButton>(R.id.btnRefreshNews)
        val tvUpdated = view.findViewById<TextView>(R.id.tvNewsUpdated)
        val recycler = view.findViewById<RecyclerView>(R.id.newsRecycler)

        val countryLabels = listOf("All", "Italy", "USA", "UK", "France", "Germany", "Spain", "Global")
        val countryCodes = mapOf(
            "All" to "all",
            "Italy" to "it",
            "USA" to "us",
            "UK" to "uk",
            "France" to "fr",
            "Germany" to "de",
            "Spain" to "es",
            "Global" to "global"
        )

        val categoryLabels = listOf(
            "All",
            "General",
            "Gaming",
            "Politics",
            "Technology",
            "Business",
            "Sports",
            "Entertainment"
        )
        val categoryCodes = mapOf(
            "All" to "all",
            "General" to "general",
            "Gaming" to "gaming",
            "Politics" to "politics",
            "Technology" to "technology",
            "Business" to "business",
            "Sports" to "sports",
            "Entertainment" to "entertainment"
        )

        dropCountry.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, countryLabels)
        )
        dropCategory.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categoryLabels)
        )

        dropCountry.setText("All", false)
        dropCategory.setText("All", false)

        adapter = NewsAdapter(emptyList()) { item ->
            val activity = activity
            if (activity is MainActivity) {
                activity.performAssistantAction("open_url", item.url)
            }
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        dropCountry.setOnItemClickListener { _, _, position, _ ->
            val label = countryLabels[position]
            selectedCountry = countryCodes[label] ?: "all"
            applyFilters(tvUpdated)
        }

        dropCategory.setOnItemClickListener { _, _, position, _ ->
            val label = categoryLabels[position]
            selectedCategory = categoryCodes[label] ?: "all"
            applyFilters(tvUpdated)
        }

        btnRefresh.setOnClickListener {
            refreshNews(tvUpdated)
        }

        val cached = repository.getCachedFeed()
        if (cached != null) {
            fullFeed = cached
            tvUpdated.text = "Updated: ${cached.updatedAt}"
            applyFilters(tvUpdated)
        }

        refreshNews(tvUpdated)
    }

    private fun refreshNews(tvUpdated: TextView) {
    thread {
        val remote = repository.fetchRemoteFeed()
        activity?.runOnUiThread {
            if (!isAdded) return@runOnUiThread

            if (remote == null) {
                Toast.makeText(
                    requireContext(),
                    "News fetch failed",
                    Toast.LENGTH_LONG
                ).show()
                return@runOnUiThread
            }

            fullFeed = remote
            tvUpdated.text = "Updated: ${remote.updatedAt}"
            applyFilters(tvUpdated)

            Toast.makeText(
                requireContext(),
                "Loaded ${remote.items.size} news",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    }

    private fun applyFilters(tvUpdated: TextView) {
        val feed = fullFeed ?: return
        val filtered = repository.filter(feed.items, selectedCountry, selectedCategory)
        adapter.updateData(filtered)

        if (filtered.isEmpty()) {
            tvUpdated.text = "${tvUpdated.text}\nNo news for current filters"
        }
    }
}
