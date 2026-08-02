package com.example.utils

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AnalysisResult(
    val summary: String,
    val pattern: String,
    val nextStep: String,
    val hashtags: List<String>?
)

data class CycleAnalysisResult(
    val insights: List<String>,
    val patterns: List<String>,
    val suggestions: List<String>
)

object GeminiApiService {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun cleanJson(responseText: String): String {
        var cleaned = responseText.trim()
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        }
        return cleaned
    }

    suspend fun analyzeCycle(cycleDataJson: String): CycleAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY") {
            throw Exception("API key is not configured")
        }

        val systemInstructionText = """
            You are a supportive cycle wellness companion. Analyze this user's recent cycle
            data and return a JSON object with:
            insights (array of 2-3 string observations, plain text without **bold** or asterisks),
            patterns (array of 2 string pattern observations, plain text without **bold** or asterisks),
            suggestions (array of 3 specific wellness suggestions, plain text without **bold** or asterisks).
            IMPORTANT: Return ONLY raw JSON. Do NOT wrap in markdown code blocks. No asterisks or markdown in strings.
        """.trimIndent()

        val requestUserPrompt = "Data: $cycleDataJson"

        val requestJson = JSONObject().apply {
            val systemParts = JSONArray().put(JSONObject().put("text", systemInstructionText))
            put("systemInstruction", JSONObject().put("parts", systemParts))
            
            val userParts = JSONArray().put(JSONObject().put("text", requestUserPrompt))
            val contentsArray = JSONArray().put(JSONObject().put("parts", userParts))
            put("contents", contentsArray)

            put("generationConfig", JSONObject().put("responseMimeType", "application/json"))
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
            .post(requestBody)
            .build()

        var retryCount = 0
        while (retryCount < 2) {
            try {
                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw java.lang.Exception("HTTP Error: ${response.code}")
                    }
                    val bodyString = response.body?.string() ?: throw Exception("Empty body")
                    val root = JSONObject(bodyString)
                    val candidates = root.optJSONArray("candidates")
                    val firstCandidate = candidates?.optJSONObject(0)
                    val outContent = firstCandidate?.optJSONObject("content")
                    val parts = outContent?.optJSONArray("parts")
                    val textStr = parts?.optJSONObject(0)?.optString("text")

                    if (!textStr.isNullOrBlank()) {
                        val cleaned = cleanJson(textStr)
                        val parsed = JSONObject(cleaned)
                        
                        fun parseArray(key: String): List<String> {
                            val array = parsed.optJSONArray(key)
                            val list = mutableListOf<String>()
                            if (array != null) {
                                for (i in 0 until array.length()) {
                                    val cleaned = array.getString(i)
                                        .replace("**", "")
                                        .replace("*", "")
                                        .replace("`", "")
                                        .replace("#", "")
                                        .trim()
                                    list.add(cleaned)
                                }
                            }
                            return list
                        }
                        
                        return@withContext CycleAnalysisResult(
                            insights = parseArray("insights"),
                            patterns = parseArray("patterns"),
                            suggestions = parseArray("suggestions")
                        )
                    }
                    throw Exception("Empty response from AI")
                }
            } catch (e: Exception) {
                retryCount++
                if (retryCount >= 2) throw e
            }
        }
        throw Exception("Failed after retries")
    }

    suspend fun getReflectiveSummary(content: String, mood: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY") {
            throw Exception("API key is not configured")
        }

        val systemInstructionText = """
            You are a compassionate emotional wellness coach. Analyze the user's journal
            entry and mood, and return a brief, supportive, and reflective daily summary (1-2 sentences).
            IMPORTANT: Return ONLY the summary text, no JSON or markdown.
        """.trimIndent()

        val userPrompt = "Mood: ${mood}\nJournal entry: $content"

        val requestJson = JSONObject().apply {
            val systemParts = JSONArray().put(JSONObject().put("text", systemInstructionText))
            put("systemInstruction", JSONObject().put("parts", systemParts))
            
            val userParts = JSONArray().put(JSONObject().put("text", userPrompt))
            val contentsArray = JSONArray().put(JSONObject().put("parts", userParts))
            put("contents", contentsArray)
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
            .post(requestBody)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw java.lang.Exception("HTTP Error: ${response.code}")
            }
            val bodyString = response.body?.string() ?: throw Exception("Empty body")
            val root = JSONObject(bodyString)
            val candidates = root.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val outContent = firstCandidate?.optJSONObject("content")
            val parts = outContent?.optJSONArray("parts")
            val textStr = parts?.optJSONObject(0)?.optString("text")

            textStr?.replace("**", "")?.replace("*", "")?.trim() ?: "Keep going, you're doing great!"
        }
    }

    suspend fun analyzeEntry(content: String, mood: String): AnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY") {
            throw Exception("API key is not configured")
        }

        val systemInstructionText = """
            You are a compassionate emotional wellness coach. Analyze the user's journal
            entry and return a JSON object with exactly these fields:
            summary (string, 1-2 sentences, observational, warm tone, plain text without **bold** or asterisks),
            pattern (string, 1 sentence, identifies an emotional pattern, plain text without **bold** or asterisks),
            nextStep (string, 1 specific actionable suggestion for today, plain text without **bold** or asterisks),
            hashtags (array of 3 strings, no # symbol, lowercase, relevant themes).
            IMPORTANT: Return ONLY raw JSON. Do NOT wrap in markdown code blocks. No bold or asterisks syntax in text values.
        """.trimIndent()

        val userPrompt = "Mood: ${mood}\nJournal entry: $content"

        val requestJson = JSONObject().apply {
            val systemParts = JSONArray().put(JSONObject().put("text", systemInstructionText))
            put("systemInstruction", JSONObject().put("parts", systemParts))
            
            val userParts = JSONArray().put(JSONObject().put("text", userPrompt))
            val contentsArray = JSONArray().put(JSONObject().put("parts", userParts))
            put("contents", contentsArray)

            put("generationConfig", JSONObject().put("responseMimeType", "application/json"))
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
            .post(requestBody)
            .build()

        var retryCount = 0
        while (retryCount < 2) {
            try {
                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw java.lang.Exception("HTTP Error: ${response.code}")
                    }
                    val bodyString = response.body?.string() ?: throw Exception("Empty body")
                    val root = JSONObject(bodyString)
                    val candidates = root.optJSONArray("candidates")
                    val firstCandidate = candidates?.optJSONObject(0)
                    val outContent = firstCandidate?.optJSONObject("content")
                    val parts = outContent?.optJSONArray("parts")
                    val textStr = parts?.optJSONObject(0)?.optString("text")

                    if (!textStr.isNullOrBlank()) {
                        val cleaned = cleanJson(textStr)
                        val parsed = JSONObject(cleaned)
                        val tagsArray = parsed.optJSONArray("hashtags")
                        val tagsList = mutableListOf<String>()
                        if (tagsArray != null) {
                            for (i in 0 until tagsArray.length()) {
                                tagsList.add(tagsArray.getString(i))
                            }
                        }
                        
                        fun stripMarkdown(s: String): String = s.replace("**", "").replace("*", "").replace("`", "").trim()
                        return@withContext AnalysisResult(
                            summary = stripMarkdown(parsed.optString("summary", "")),
                            pattern = stripMarkdown(parsed.optString("pattern", "")),
                            nextStep = stripMarkdown(parsed.optString("nextStep", "")),
                            hashtags = tagsList.map { stripMarkdown(it) }
                        )
                    }
                    throw Exception("Empty response from AI")
                }
            } catch (e: Exception) {
                retryCount++
                if (retryCount >= 2) throw e
            }
        }
        throw Exception("Failed after retries")
    }
}
