package com.xdustatom.auryxbrowser.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import com.xdustatom.auryxbrowser.databinding.FragmentAssistantBinding
import com.xdustatom.auryxbrowser.utils.AuryxAssistant

class AssistantFragment(
    private val onAction: (String, String?) -> Unit
) : Fragment() {

    private var _binding: FragmentAssistantBinding? = null
    private val binding get() = _binding!!
    private lateinit var assistant: AuryxAssistant

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        binding.btnSend.setOnClickListener { sendMessage() }

        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }

        binding.btnOpenWebsite.setOnClickListener {
            binding.etMessage.setText("open google.com")
            sendMessage()
        }

        binding.btnSearch.setOnClickListener {
            binding.etMessage.setText("search android news")
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
        binding.tvConversation.text =
            "Auryx: ${assistant.getWelcomeMessage()}\n\n" +
            "Examples:\n" +
            "• open youtube.com\n" +
            "• search weather milan\n" +
            "• open bookmarks\n" +
            "• open history\n" +
            "• help bookmarks"
    }

    private fun sendMessage() {
        val message = binding.etMessage.text.toString().trim()
        if (message.isEmpty()) return

        appendMessage("You", message)
        binding.etMessage.setText("")

        val response = assistant.processQuery(message)
        appendMessage("Auryx", response.message)

        response.action?.let { action ->
            when (action) {
                "open_url" -> onAction("open_url", response.data)
                "search" -> onAction("search", response.data)
                "open_settings" -> onAction("open_settings", null)
                "open_bookmarks" -> onAction("open_bookmarks", null)
                "open_history" -> onAction("open_history", null)
                "new_tab" -> onAction("new_tab", null)
            }
        }
    }

    private fun appendMessage(sender: String, text: String) {
        val current = binding.tvConversation.text.toString()
        val next = if (current.isBlank()) {
            "$sender: $text"
        } else {
            "$current\n\n$sender: $text"
        }
        binding.tvConversation.text = next

        binding.scrollView.post {
            binding.scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
