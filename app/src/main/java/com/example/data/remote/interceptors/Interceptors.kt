package com.example.data.remote.interceptors

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.database.CareeronixLocalDatabase
import com.example.data.local.entities.ExperienceEntity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

class AuthTokenInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val prefs = context.getSharedPreferences("careeronix_auth", Context.MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null)

        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        if (token != null) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}

class MockInterceptor(private val context: Context) : Interceptor {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        val method = request.method

        Log.d("CleanMockInterceptor", "Incoming Intercepted Call: $method - $url")

        // Short sleep to represent realistic network delay
        Thread.sleep(600)

        return when {
            url.contains("api/auth/login") && method == "POST" -> {
                handleLogin(request)
            }
            url.contains("api/experience") && method == "GET" -> {
                handleGetExperiences()
            }
            url.contains("api/experience") && method == "POST" -> {
                handlePostExperience(request)
            }
            url.contains("api/experience") && method == "PUT" -> {
                val segment = url.substringAfterLast("/")
                val id = segment.toIntOrNull() ?: 0
                handlePutExperience(id, request)
            }
            url.contains("api/experience") && method == "DELETE" -> {
                val segment = url.substringAfterLast("/")
                val id = segment.toIntOrNull() ?: 0
                handleDeleteExperience(id)
            }
            url.contains("api/resume/upload") && method == "POST" -> {
                val role = request.url.queryParameter("role") ?: "Frontend Developer"
                handleResumeScan(role)
            }
            url.contains("api/interview/submit") && method == "POST" -> {
                handleInterviewSubmit(request)
            }
            else -> {
                chain.proceed(request)
            }
        }
    }

    private fun okResponse(json: String, request: Request): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(json.toResponseBody("application/json".toMediaTypeOrNull()))
            .build()
    }

    private fun handleLogin(request: Request): Response {
        val bodyStr = request.body?.let { bodyToString(it) } ?: "{}"
        val json = JSONObject(bodyStr)
        val email = json.optString("email", "candidate@careeronix.ai")
        val name = json.optString("name", "Aspiring candidate")

        val token = "jwt_clean_token_" + UUID.randomUUID().toString()

        val responseJson = JSONObject().apply {
            put("token", token)
            put("name", name)
            put("email", email)
            put("targetRole", "Frontend Developer")
            put("level", 1)
            put("xp", 180)
            put("jobReadiness", 50)
        }

        return okResponse(responseJson.toString(), request)
    }

    private fun handleGetExperiences(): Response {
        val database = CareeronixLocalDatabase.getDatabase(context)
        val records = database.experienceEntityDao().getExperiencesSync()

        val jsonArray = JSONArray()
        for (rec in records) {
            val obj = JSONObject().apply {
                put("id", rec.id)
                put("title", rec.title)
                put("company", rec.company)
                put("period", rec.period)
                put("description", rec.description)
            }
            jsonArray.put(obj)
        }
        return Response.Builder()
            .request(Request.Builder().url("https://api.careeronix.ai/api/experience").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(jsonArray.toString().toResponseBody("application/json".toMediaTypeOrNull()))
            .build()
    }

    private fun handlePostExperience(request: Request): Response {
        val bodyStr = request.body?.let { bodyToString(it) } ?: "{}"
        val json = JSONObject(bodyStr)
        val title = json.optString("title", "Software Engineer Intern")
        val company = json.optString("company", "TechCorp")
        val period = json.optString("period", "June 2026 - Present")
        val description = json.optString("description", "Built React components and Node backends.")

        val database = CareeronixLocalDatabase.getDatabase(context)
        val record = ExperienceEntity(
            title = title,
            company = company,
            period = period,
            description = description
        )
        val newId = database.experienceEntityDao().insertExperienceSync(record).toInt()

        val responseObj = JSONObject().apply {
            put("id", newId)
            put("title", title)
            put("company", company)
            put("period", period)
            put("description", description)
        }
        return okResponse(responseObj.toString(), request)
    }

    private fun handlePutExperience(id: Int, request: Request): Response {
        val bodyStr = request.body?.let { bodyToString(it) } ?: "{}"
        val json = JSONObject(bodyStr)
        val title = json.optString("title", "")
        val company = json.optString("company", "")
        val period = json.optString("period", "")
        val description = json.optString("description", "")

        val database = CareeronixLocalDatabase.getDatabase(context)
        val updatedRecord = ExperienceEntity(
            id = id,
            title = title,
            company = company,
            period = period,
            description = description
        )
        database.experienceEntityDao().updateExperienceSync(updatedRecord)

        val responseObj = JSONObject().apply {
            put("id", id)
            put("title", title)
            put("company", company)
            put("period", period)
            put("description", description)
        }
        return okResponse(responseObj.toString(), request)
    }

    private fun handleDeleteExperience(id: Int): Response {
        val database = CareeronixLocalDatabase.getDatabase(context)
        database.experienceEntityDao().deleteExperienceByIdSync(id)

        val responseObj = JSONObject().apply {
            put("status", "success")
            put("message", "Deleted experience entry $id")
        }
        return Response.Builder()
            .request(Request.Builder().url("https://api.careeronix.ai/api/experience/$id").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseObj.toString().toResponseBody("application/json".toMediaTypeOrNull()))
            .build()
    }

    private fun handleResumeScan(role: String): Response {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (!apiKey.isNullOrEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    Analyze a candidate resume targeting the professional role: '$role'.
                    Output an ATS optimization analysis formatted strictly as valid JSON.
                    The JSON model schema must strictly contain these keys:
                    {
                      "atsScore": Integer (between 62 and 97),
                      "matchedSkills": Array of Strings,
                      "missingSkills": Array of Strings,
                      "strengths": Array of Strings,
                      "weaknesses": Array of Strings,
                      "suggestions": Array of Strings
                    }
                    Ensure the suggestions are highly customized for '$role'. Return ONLY the valid raw JSON object.
                """.trimIndent()

                val geminiJson = performGeminiRequest(apiKey, prompt)
                if (geminiJson != null) return okResponse(geminiJson, Request.Builder().url("https://api.careeronix.ai/api/resume/upload").build())
            } catch (e: Exception) {
                Log.e("CleanMockInterceptor", "Gemini Real-time Resume analysis failed, falling back", e)
            }
        }

        val matched = listOf("Web Design", "Git", "JavaScript", "Layouts")
        val missing = listOf("TypeScript", "CI/CD Deployment", "State Machine")
        val suggestions = listOf("Incorporate quantative statements in achievements.", "Integrate solid TypeScript references.")

        val jsonResponse = JSONObject().apply {
            put("atsScore", 74)
            put("matchedSkills", JSONArray(matched))
            put("missingSkills", JSONArray(missing))
            put("strengths", JSONArray(listOf("Clean layouts", "Correct formatting")))
            put("weaknesses", JSONArray(listOf("Lack of performance indicators")))
            put("suggestions", JSONArray(suggestions))
        }

        return okResponse(jsonResponse.toString(), Request.Builder().url("https://api.careeronix.ai/api/resume/upload").build())
    }

    private fun handleInterviewSubmit(request: Request): Response {
        val bodyStr = request.body?.let { bodyToString(it) } ?: "{}"
        val entry = JSONObject(bodyStr)
        val role = entry.optString("role", "Frontend Developer")
        val question = entry.optString("question", "")
        val answer = entry.optString("answer", "")

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!apiKey.isNullOrEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    Rate candidate's answer fairly.
                    Question: "$question"
                    Answer: "$answer"
                    Output valid JSON only with keys:
                    {
                      "overallScore": Integer (40-100),
                      "confidenceScore": Integer (40-100),
                      "communicationScore": Integer (40-100),
                      "technicalScore": Integer (40-100),
                      "feedback": String
                    }
                """.trimIndent()

                val geminiJson = performGeminiRequest(apiKey, prompt)
                if (geminiJson != null) return okResponse(geminiJson, request)
            } catch (e: Exception) {
                Log.e("CleanMockInterceptor", "Gemini Interview assessment failed, falling back", e)
            }
        }

        val jsonResponse = JSONObject().apply {
            put("overallScore", 76)
            put("confidenceScore", 80)
            put("communicationScore", 75)
            put("technicalScore", 73)
            put("feedback", "Excellent response structure. Try to add more precise quantitative metrics in your future answers.")
        }

        return okResponse(jsonResponse.toString(), request)
    }

    private fun performGeminiRequest(apiKey: String, prompt: String): String? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val contentsArray = JSONArray().apply {
            put(JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", prompt) })
                })
            })
        }

        val bodyObj = JSONObject().apply {
            put("contents", contentsArray)
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
            })
        }

        val body = RequestBody.create("application/json".toMediaTypeOrNull(), bodyObj.toString())
        val req = Request.Builder().url(url).post(body).build()

        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return null
            val respStr = resp.body?.string() ?: return null
            val respJson = JSONObject(respStr)
            val candidates = respJson.optJSONArray("candidates") ?: return null
            if (candidates.length() > 0) {
                val firstPart = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")?.get(0) as? JSONObject
                return firstPart?.optString("text")?.trim()
            }
        }
        return null
    }

    private fun bodyToString(requestBody: RequestBody): String {
        try {
            val buffer = okio.Buffer()
            requestBody.writeTo(buffer)
            return buffer.readUtf8()
        } catch (e: IOException) {
            return "{}"
        }
    }
}
