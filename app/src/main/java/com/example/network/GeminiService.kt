package com.example.network

import com.example.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// --- Data Classes ---

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>
)

@Serializable
data class Candidate(
    val content: Content
)

// --- Retrofit Service ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

// --- Helper for Summary ---

suspend fun generateMoodSummary(moodData: String): String = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isEmpty()) return@withContext "API Key missing."

    val prompt = "Based on this mood data over the last 14 days: $moodData. Provide a brief, supportive, and reflective daily summary (1-2 sentences). Do NOT use markdown bold syntax or asterisks."
    
    val request = GenerateContentRequest(
        contents = listOf(Content(
            parts = listOf(Part(text = prompt))
        ))
    )
    
    try {
        val response = RetrofitClient.service.generateContent(apiKey, request)
        val rawText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No summary available."
        cleanMarkdownText(rawText)
    } catch (e: Exception) {
        "Error generating summary: ${e.message}"
    }
}

fun cleanMarkdownText(text: String): String {
    return text
        .replace("**", "")
        .replace("*", "")
        .replace("`", "")
        .replace("#", "")
        .trim()
}
