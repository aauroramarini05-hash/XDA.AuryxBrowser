package com.xdustatom.auryxbrowser.utils

import java.net.URLEncoder

data class AssistantResponse(
    val message: String,
    val action: String? = null,
    val data: String? = null
)

class AuryxAssistant {

    fun getWelcomeMessage(): String {
        return "Hello! I'm your local browser helper. I can open websites, search on Google, YouTube, DuckDuckGo, Wikipedia and GitHub, open bookmarks, open history, and handle combined commands."
    }

    fun processQuery(query: String): AssistantResponse {
        val raw = query.trim()
        val q = raw.lowercase().trim()

        if (q.isBlank()) {
            return AssistantResponse("Write something first.")
        }

        if (q == "help" || q == "commands") {
            return AssistantResponse(
                "Try commands like:\n" +
                    "• open google.com\n" +
                    "• search android tv browser\n" +
                    "• search on youtube geolier\n" +
                    "• open youtube and search mrbeast\n" +
                    "• open wikipedia and search metaverse\n" +
                    "• open github and search auryxbrowser\n" +
                    "• open settings\n" +
                    "• open bookmarks\n" +
                    "• open history"
            )
        }

        if (q in listOf("hi", "hello", "hey", "hi auryx", "hello auryx")) {
            return AssistantResponse(
                "Hello! Try a command like \"open youtube and search music\" or \"search on wikipedia android\"."
            )
        }

        if (q.contains("thank")) {
            return AssistantResponse("You're welcome.")
        }

        if (q == "open settings" || q == "settings") {
            return AssistantResponse("Opening Settings...", "open_settings")
        }

        if (q == "open bookmarks" || q == "bookmarks") {
            return AssistantResponse("Opening Bookmarks...", "open_bookmarks")
        }

        if (q == "open history" || q == "history") {
            return AssistantResponse("Opening History...", "open_history")
        }

        if (q.contains("new tab")) {
            return AssistantResponse("Opening a new tab...", "new_tab")
        }

        val helpReply = handleHelp(q)
        if (helpReply != null) return helpReply

        val multiActionReply = handleCombinedCommands(raw, q)
        if (multiActionReply != null) return multiActionReply

        val siteSearchReply = handleSiteSearch(raw, q)
        if (siteSearchReply != null) return siteSearchReply

        val openReply = handleOpen(raw, q)
        if (openReply != null) return openReply

        val genericSearchReply = handleGenericSearch(raw, q)
        if (genericSearchReply != null) return genericSearchReply

        return AssistantResponse(
            "I didn't understand that.\n" +
                "Try:\n" +
                "• open youtube.com\n" +
                "• search weather today\n" +
                "• search on youtube gaming news\n" +
                "• open google and search meteo milano\n" +
                "• open bookmarks"
        )
    }

    private fun handleHelp(q: String): AssistantResponse? {
        return when {
            q.contains("bookmark") ->
                AssistantResponse("To add a bookmark, open a page, tap the menu, then tap Add bookmark.")

            q.contains("history") ->
                AssistantResponse("Browsing history is available in the History section from the bottom bar.")

            q.contains("settings") ->
                AssistantResponse("Settings lets you change desktop mode, home URL, and clear history or bookmarks.")

            q.contains("youtube") ->
                AssistantResponse("You can say: \"search on youtube music\" or \"open youtube and search mrbeast\".")

            q.contains("google") ->
                AssistantResponse("You can say: \"search weather today\" or \"open google and search meteo milano\".")

            q.contains("wikipedia") ->
                AssistantResponse("You can say: \"search on wikipedia android\" or \"open wikipedia and search metaverse\".")

            q.contains("github") ->
                AssistantResponse("You can say: \"search on github browser\" or \"open github and search auryxbrowser\".")

            else -> null
        }
    }

    private fun handleCombinedCommands(raw: String, q: String): AssistantResponse? {
        val combinedPrefixes = listOf("open ", "go to ", "visit ")
        val hasOpenPrefix = combinedPrefixes.any { q.startsWith(it) }
        if (!hasOpenPrefix) return null

        val cleaned = q
            .removePrefix("open ")
            .removePrefix("go to ")
            .removePrefix("visit ")
            .trim()

        val separators = listOf(" and search ", " then search ", " & search ")
        for (separator in separators) {
            if (cleaned.contains(separator)) {
                val parts = cleaned.split(separator, limit = 2)
                val site = parts.getOrNull(0)?.trim().orEmpty()
                val searchQuery = parts.getOrNull(1)?.trim().orEmpty()

                if (site.isBlank() || searchQuery.isBlank()) {
                    return AssistantResponse("Write both a site and something to search.")
                }

                val url = buildSiteSearchUrl(site, searchQuery)
                return AssistantResponse(
                    "Opening ${siteDisplayName(site)} and searching for \"$searchQuery\"...",
                    "open_url",
                    url
                )
            }
        }

        return null
    }

    private fun handleSiteSearch(raw: String, q: String): AssistantResponse? {
        val patterns = listOf(
            "search on ",
            "find on ",
            "look up on "
        )

        for (prefix in patterns) {
            if (q.startsWith(prefix)) {
                val rest = raw.substring(prefix.length).trim()
                val lowerRest = rest.lowercase()

                val splitters = listOf(" for ", " ", ":")
                for (splitter in splitters) {
                    val idx = lowerRest.indexOf(splitter)
                    if (idx > 0) {
                        val site = rest.substring(0, idx).trim()
                        val searchQuery = rest.substring(idx + splitter.length).trim()

                        if (site.isNotBlank() && searchQuery.isNotBlank()) {
                            val url = buildSiteSearchUrl(site, searchQuery)
                            return AssistantResponse(
                                "Searching on ${siteDisplayName(site)} for \"$searchQuery\"...",
                                "open_url",
                                url
                            )
                        }
                    }
                }
            }
        }

        if (q.startsWith("youtube ")) {
            val searchQuery = raw.substringAfter(" ", "").trim()
            if (searchQuery.isNotBlank()) {
                return AssistantResponse(
                    "Searching on YouTube for \"$searchQuery\"...",
                    "open_url",
                    buildYouTubeSearchUrl(searchQuery)
                )
            }
        }

        return null
    }

    private fun handleOpen(raw: String, q: String): AssistantResponse? {
        if (!(q.startsWith("open ") || q.startsWith("go to ") || q.startsWith("visit "))) {
            return null
        }

        val value = raw
            .removePrefix("open ")
            .removePrefix("Open ")
            .removePrefix("go to ")
            .removePrefix("Go to ")
            .removePrefix("visit ")
            .removePrefix("Visit ")
            .trim()

        if (value.isBlank()) {
            return AssistantResponse("Write a website or destination to open.")
        }

        val special = when (value.lowercase()) {
            "google" -> "https://www.google.com/"
            "youtube" -> "https://m.youtube.com/"
            "duckduckgo" -> "https://duckduckgo.com/"
            "wikipedia" -> "https://www.wikipedia.org/"
            "github" -> "https://github.com/"
            else -> null
        }

        if (special != null) {
            return AssistantResponse("Opening ${siteDisplayName(value)}...", "open_url", special)
        }

        val finalUrl = normalizeToUrl(value)
        return AssistantResponse("Opening $finalUrl...", "open_url", finalUrl)
    }

    private fun handleGenericSearch(raw: String, q: String): AssistantResponse? {
        if (
            q.startsWith("search ") ||
            q.startsWith("find ") ||
            q.startsWith("look up ")
        ) {
            val searchQuery = raw
                .removePrefix("search ")
                .removePrefix("Search ")
                .removePrefix("find ")
                .removePrefix("Find ")
                .removePrefix("look up ")
                .removePrefix("Look up ")
                .trim()

            return if (searchQuery.isBlank()) {
                AssistantResponse("Write something to search for.")
            } else {
                AssistantResponse(
                    "Searching for \"$searchQuery\"...",
                    "search",
                    searchQuery
                )
            }
        }

        return null
    }

    private fun buildSiteSearchUrl(site: String, query: String): String {
        val s = site.lowercase().trim()
        val encoded = enc(query)

        return when {
            s.contains("youtube") -> buildYouTubeSearchUrl(query)
            s.contains("google") -> "https://www.google.com/search?q=$encoded"
            s.contains("duckduckgo") || s == "ddg" -> "https://duckduckgo.com/?q=$encoded"
            s.contains("wikipedia") -> "https://en.wikipedia.org/w/index.php?search=$encoded"
            s.contains("github") -> "https://github.com/search?q=$encoded"
            s.contains("bing") -> "https://www.bing.com/search?q=$encoded"
            else -> {
                val domain = if (s.contains(".")) s else "$s.com"
                "https://www.google.com/search?q=${enc("site:$domain $query")}"
            }
        }
    }

    private fun buildYouTubeSearchUrl(query: String): String {
        return "https://m.youtube.com/results?search_query=${enc(query)}"
    }

    private fun normalizeToUrl(value: String): String {
        val trimmed = value.trim()

        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }

        return if (trimmed.contains(".")) {
            "https://$trimmed"
        } else {
            "https://$trimmed.com"
        }
    }

    private fun siteDisplayName(site: String): String {
        return when (site.lowercase().trim()) {
            "youtube" -> "YouTube"
            "google" -> "Google"
            "duckduckgo", "ddg" -> "DuckDuckGo"
            "wikipedia" -> "Wikipedia"
            "github" -> "GitHub"
            else -> site.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    private fun enc(text: String): String {
        return URLEncoder.encode(text, "UTF-8")
    }
}
