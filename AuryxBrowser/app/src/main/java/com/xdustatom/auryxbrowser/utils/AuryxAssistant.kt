package com.xdustatom.auryxbrowser.utils

data class AssistantResponse(
    val message: String,
    val action: String? = null,
    val data: String? = null
)

class AuryxAssistant {

    private val helpResponses = mapOf(
        "bookmark" to "To add a bookmark, tap the menu button (⋮) and select 'Add to Bookmarks'. You can view all bookmarks from the bottom navigation bar.",
        "history" to "Your browsing history is accessible from the bottom navigation bar. Tap 'History' to see all visited pages. You can clear history in Settings.",
        "download" to "When you tap a downloadable file, AuryxBrowser will automatically start the download. Check your downloads from the menu.",
        "tab" to "Tap the tabs button in the top bar to see all open tabs. You can open new tabs, switch between them, or close them from there.",
        "search" to "Type in the URL bar or home search bar to search. DuckDuckGo is the default search engine. You can change it in Settings.",
        "desktop" to "Enable Desktop Mode from the menu or Settings to view desktop versions of websites.",
        "javascript" to "JavaScript can be enabled or disabled in Settings under the Browser section.",
        "cache" to "Clear your browser cache in Settings under the Privacy section.",
        "settings" to "Access Settings from the bottom navigation bar to customize your browser experience."
    )

    fun getWelcomeMessage(): String {
        return """Hello! I'm Auryx, your browser assistant. I can help you with:

• Opening websites (try: "open google.com")
• Searching the web (try: "search android news")
• Browser features (try: "how to bookmark")
• Navigation (try: "open settings")

What would you like to do?"""
    }

    fun processQuery(query: String): AssistantResponse {
        val lowerQuery = query.lowercase().trim()

        // Open URL commands
        if (lowerQuery.startsWith("open ") || lowerQuery.startsWith("go to ") || lowerQuery.startsWith("visit ")) {
            val url = lowerQuery
                .removePrefix("open ")
                .removePrefix("go to ")
                .removePrefix("visit ")
                .trim()
            
            return if (url == "settings") {
                AssistantResponse("Opening Settings...", "open_settings")
            } else if (url == "bookmarks") {
                AssistantResponse("Opening Bookmarks...", "open_bookmarks")
            } else if (url == "history") {
                AssistantResponse("Opening History...", "open_history")
            } else {
                val finalUrl = if (url.contains(".")) url else "$url.com"
                AssistantResponse("Opening $finalUrl...", "open_url", finalUrl)
            }
        }

        // Search commands
        if (lowerQuery.startsWith("search ") || lowerQuery.startsWith("find ") || lowerQuery.startsWith("look up ")) {
            val searchQuery = lowerQuery
                .removePrefix("search ")
                .removePrefix("find ")
                .removePrefix("look up ")
                .trim()
            return AssistantResponse("Searching for \"$searchQuery\"...", "search", searchQuery)
        }

        // New tab
        if (lowerQuery.contains("new tab") || lowerQuery.contains("open tab")) {
            return AssistantResponse("Opening a new tab...", "new_tab")
        }

        // Help queries
        if (lowerQuery.contains("how to") || lowerQuery.contains("help") || lowerQuery.contains("what is")) {
            for ((keyword, response) in helpResponses) {
                if (lowerQuery.contains(keyword)) {
                    return AssistantResponse(response)
                }
            }
            return AssistantResponse(getHelpMessage())
        }

        // Greetings
        if (lowerQuery in listOf("hi", "hello", "hey", "hi there", "hello there")) {
            return AssistantResponse("Hello! How can I help you today? Try asking me to open a website or search for something.")
        }

        // Thanks
        if (lowerQuery.contains("thank") || lowerQuery.contains("thanks")) {
            return AssistantResponse("You're welcome! Let me know if you need anything else.")
        }

        // Default response
        return AssistantResponse("I'm not sure how to help with that. Try:\n• \"open google.com\"\n• \"search weather today\"\n• \"how to bookmark\"\n• \"open settings\"")
    }

    private fun getHelpMessage(): String {
        return """Here's what I can help you with:

📌 Bookmarks - Save and manage your favorite pages
📜 History - View and clear browsing history
📥 Downloads - Manage downloaded files
🗂️ Tabs - Open and manage multiple tabs
🔍 Search - Search the web with DuckDuckGo
🖥️ Desktop Mode - View desktop versions of sites
⚙️ Settings - Customize your browser

Just ask about any of these topics!"""
    }
}
