package com.xdustatom.auryxbrowser.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.xdustatom.auryxbrowser.databinding.FragmentAssistantBinding
import com.xdustatom.auryxbrowser.utils.AuryxAssistant

class AssistantFragment(
    private val onAction: (String, String?) -> Unit
) : Fragment() {

    private var _binding: FragmentAssistantBinding? = null
    private val binding get() = _binding!!
    private lateinit var assistant: AuryxAssistant

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAssistantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        assistant = AuryxAssistant()
        setupUI()
        showWelcome()
    }

    private fun setupUI() {
        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }

        // Quick action buttons
        binding.btnOpenWebsite.setOnClickListener {
            binding.etMessage.setText("open google.com")
            sendMessage()
        }

        binding.btnSearch.setOnClickListener {
            binding.etMessage.setText("search android tips")
            sendMessage()
        }

        binding.btnSettings.setOnClickListener {
            binding.etMessage.setText("open settings")
            sendMessage()
        }

        binding.btnHelp.setOnClickListener {
            binding.etMessage.setText("help")
            sendMessage()
        }
    }

    private fun showWelcome() {
        addMessage("Auryx", assistant.getWelcomeMessage())
    }

    private fun sendMessage() {
        val message = binding.etMessage.text.toString().trim()
        if (message.isEmpty()) return

        addMessage("You", message)
        binding.etMessage.setText("")

        val response = assistant.processQuery(message)
        addMessage("Auryx", response.message)

        // Execute action if present
        response.action?.let { action ->
            when (action) {
                "open_url" -> response.data?.let { url -> onAction("open_url", url) }
                "search" -> response.data?.let { query -> onAction("search", query) }
                "open_settings" -> onAction("open_settings", null)
                "open_bookmarks" -> onAction("open_bookmarks", null)
                "open_history" -> onAction("open_history", null)
                "new_tab" -> onAction("new_tab", null)
                else -> { /* Unknown action, ignore */ }
            }
        }
    }

    private fun addMessage(sender: String, text: String) {
        val currentText = binding.tvConversation.text.toString()
        val newText = if (currentText.isEmpty()) {
            "$sender: $text"
        } else {
            "$currentText\n\n$sender: $text"
        }
        binding.tvConversation.text = newText
        binding.scrollView.post {
            binding.scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
