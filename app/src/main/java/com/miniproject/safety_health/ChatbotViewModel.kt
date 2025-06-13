package com.miniproject.safety_health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class ChatbotViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("assistant", "Hello! I'm your safety and health assistant. How can I help you today?")
    ))
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Configure HTTP client with timeout and JSON support
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) { json() }
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            connectTimeoutMillis = 30_000
        }
        expectSuccess = true
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _messages.value = _messages.value + ChatMessage("user", message)

                val response = client.post("https://api.llm7.io/v1/chat/completions") {
                    contentType(ContentType.Application.Json)
                    header("Authorization", "Bearer unused")
                    setBody(
                        LLM7Request(
                            model = "deepseek-r1",
                            messages = _messages.value.map {
                                LLM7Message(it.role, it.content)
                            }
                        )
                    )
                }.body<LLM7Response>()

                val reply = response.choices.first().message.content
                _messages.value = _messages.value + ChatMessage("assistant", reply)
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is ClientRequestException -> "Server error: ${e.response.status}"
                    is ServerResponseException -> "Server error: ${e.response.status}"
                    else -> "Network error: ${e.message ?: "Please check your connection"}"
                }
                _messages.value = _messages.value + ChatMessage("system", errorMessage)
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