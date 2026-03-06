package com.xdustatom.auryxbrowser.utils

data class AssistantResponse(
    val message: String,
    val action: String? = null,
    val data: String? = null
)

class AuryxAssistant {

    fun getWelcomeMessage(): String {
        return "Hello! I'm your local browser helper. I can open websites, search the web, open bookmarks, open history, and explain browser features."
    }

    fun processQuery(query: String): AssistantResponse {
        val q = query.lowercase().trim()

        if (q == "help") {
            return AssistantResponse(
                "Try commands like:\n" +
                "• open google.com\n" +
                "• search android tv browser\n" +
                "• open settings\n" +
                "• open bookmarks\n" +
                "• open history\n" +
                "• help bookmarks"
            )
        }

        if (q.startsWith("open ") || q.startsWith("go to ") || q.startsWith("visit ")) {
            val value = q
                .removePrefix("open ")
                .removePrefix("go to ")
                .removePrefix("visit ")
                .trim()

            return when (value) {
                "settings" -> AssistantResponse("Opening Settings...", "open_settings")
                "bookmarks" -> AssistantResponse("Opening Bookmarks...", "open_bookmarks")
                "history" -> AssistantResponse("Opening History...", "open_history")
                else -> {
                    val finalUrl = if (
                        value.startsWith("http://") || value.startsWith("https://")
                    ) {
                        value
                    } else if (value.contains(".")) {
                        "https://$value"
                    } else {
                        "https://$value.com"
                    }

                    AssistantResponse("Opening $finalUrl...", "open_url", finalUrl)
                }
            }
        }

        if (q.startsWith("search ") || q.startsWith("find ") || q.startsWith("look up ")) {
            val searchQuery = q
                .removePrefix("search ")
                .removePrefix("find ")
                .removePrefix("look up ")
                .trim()

            return if (searchQuery.isBlank()) {
                AssistantResponse("Write something to search for.")
            } else {
                AssistantResponse("Searching for \"$searchQuery\"...", "search", searchQuery)
            }
        }

        if (q.contains("new tab")) {
            return AssistantResponse("Opening a new tab...", "new_tab")
        }

        if (q.contains("bookmark")) {
            return AssistantResponse("To add a bookmark, open a page, tap the menu, then tap Add bookmark.")
        }

        if (q.contains("history")) {
            return AssistantResponse("Browsing history is available in the History section from the bottom bar.")
        }

        if (q.contains("settings")) {
            return AssistantResponse("Settings lets you change desktop mode, home URL, and clear history or bookmarks.")
        }

        if (q in listOf("hi", "hello", "hey")) {
            return AssistantResponse("Hello! Try a command like \"open google.com\" or \"search android news\".")
        }

        if (q.contains("thank")) {
            return AssistantResponse("You're welcome.")
        }

        return AssistantResponse(
            "I didn't understand that.\n" +
            "Try:\n" +
            "• open youtube.com\n" +
            "• search weather today\n" +
            "• open settings\n" +
            "• open bookmarks"
        )
    }
}
