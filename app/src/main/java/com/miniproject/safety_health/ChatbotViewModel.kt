package com.miniproject.safety_health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class ChatbotViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _messages.value = _messages.value + ChatMessage("user", message)

                val response = client.post("https://api.llm7.io/v1/chat/completions") {

                    contentType(ContentType.Application.Json)
                    setBody(LLM7Request(
                        model = "gpt-4.1-nano",
                        messages = _messages.value.map {
                            LLM7Message(it.role, it.content)
                        }
                    ))
                }.body<LLM7Response>() // Add .body() to deserialize the response

                val reply = response.choices.first().message.content
                _messages.value += ChatMessage("assistant", reply)
            } catch (e: Exception) {
                _messages.value += ChatMessage("system", "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    @Serializable
    data class LLM7Request(
        val model: String,
        val messages: List<LLM7Message>
    )

    @Serializable
    data class LLM7Message(
        val role: String,
        val content: String
    )

    @Serializable
    data class LLM7Response(
        val choices: List<LLM7Choice>
    )

    @Serializable
    data class LLM7Choice(
        val message: LLM7Message
    )
}

data class ChatMessage(
    val role: String,
    val content: String
)