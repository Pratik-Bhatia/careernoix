package com.example.api

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.CareeronixDatabase
import com.example.data.ExperienceRecord
import com.example.data.ResumeRecord
import com.example.data.InterviewRecord
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

// --- DATA SCHEMAS ---

data class LoginRequest(
    val email: String,
    val name: String
)

data class AuthResponse(
    val token: String,
    val name: String,
    val email: String,
    val targetRole: String,
    val level: Int,
    val xp: Int,
    val jobReadiness: Int
)

data class ExperienceEntry(
    val id: Int,
    val title: String,
    val company: String,
    val period: String,
    val description: String
)

data class AtsScanResult(
    val atsScore: Int,
    val matchedSkills: List<String>,
    val missingSkills: List<String>,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val suggestions: List<String>
)

data class InterviewEvaluation(
    val overallScore: Int,
    val confidenceScore: Int,
    val communicationScore: Int,
    val technicalScore: Int,
    val feedback: String
)

data class InterviewSubmission(
    val role: String,
    val question: String,
    val answer: String
)

// --- RETROFIT SERVICE INTERFACE ---

interface CareeronixApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("api/experience")
    suspend fun getExperiences(): List<ExperienceEntry>

    @POST("api/experience")
    suspend fun addExperience(@Body entry: ExperienceEntry): ExperienceEntry

    @PUT("api/experience/{id}")
    suspend fun updateExperience(@Path("id") id: Int, @Body entry: ExperienceEntry): ExperienceEntry

    @DELETE("api/experience/{id}")
    suspend fun deleteExperience(@Path("id") id: Int): ResponseBody

    @Multipart
    @POST("api/resume/upload")
    suspend fun uploadResume(
        @Part file: MultipartBody.Part,
        @Query("role") role: String
    ): AtsScanResult

    @POST("api/interview/submit")
    suspend fun submitInterview(@Body submission: InterviewSubmission): InterviewEvaluation
}

// --- SECURE AUTHENTICATION RECONCILIATION INTERCEPTOR ---

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

// --- INTELLIGENT COMPREHENSIVE LOCAL MOCK SERVER & AI PROXY INTERCEPTOR ---

class MockInterceptor(private val context: Context) : Interceptor {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        val method = request.method

        Log.d("MockInterceptor", "Incoming Intercepted Call: $method - $url")

        // Short sleep to represent realistic network delay
        Thread.sleep(800)

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
                // Pass-through fallback
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

        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." + UUID.randomUUID().toString()

        val responseJson = JSONObject().apply {
            put("token", token)
            put("name", name)
            put("email", email)
            put("targetRole", "Frontend Developer")
            put("level", 1)
            put("xp", 150)
            put("jobReadiness", 45)
        }

        return okResponse(responseJson.toString(), request)
    }

    private fun handleGetExperiences(): Response {
        val database = CareeronixDatabase.getDatabase(context)
        val records = database.experienceDao().getExperiencesSync()

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

        val database = CareeronixDatabase.getDatabase(context)
        val record = ExperienceRecord(
            title = title,
            company = company,
            period = period,
            description = description
        )
        val newId = database.experienceDao().insertExperienceSync(record).toInt()

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

        val database = CareeronixDatabase.getDatabase(context)
        val updatedRecord = ExperienceRecord(
            id = id,
            title = title,
            company = company,
            period = period,
            description = description
        )
        database.experienceDao().updateExperienceSync(updatedRecord)

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
        val database = CareeronixDatabase.getDatabase(context)
        database.experienceDao().deleteExperienceByIdSync(id)

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
                      "atsScore": Integer (between 60 and 97),
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
                Log.e("MockInterceptor", "Gemini Real-time Resume analysis failed, falling back", e)
            }
        }

        // --- RULE-BASED ROBUST LOCAL ATS FALLBACK ---
        val matched: List<String>
        val missing: List<String>
        val weaknesses: List<String>
        val suggestions: List<String>
        val score: Int

        when (role) {
            "Frontend Developer" -> {
                score = 78
                matched = listOf("HTML5", "CSS3", "JavaScript", "React", "Git")
                missing = listOf("TypeScript", "Next.js", "Redux Toolkit", "Jest Unit Testing")
                weaknesses = listOf("Lack of rigorous modern TypeScript implementations", "No performance benchmark metrics")
                suggestions = listOf(
                    "Convert description points into bullet parameters quantifying page optimizations (+25% load speeds).",
                    "Add an explicit section mapping Next.js App Router and state modules."
                )
            }
            "Data Analyst" -> {
                score = 81
                matched = listOf("SQL", "Python", "Excel formulas", "Pandas")
                missing = listOf("Tableau charts dashboards", "A/B Testing significance", "PySpark", "BigQuery")
                weaknesses = listOf("No predictive modeling representations", "Minimal descriptive statistic details")
                suggestions = listOf(
                    "Replace high-level entries with clear SQL server optimizations and database indexes created.",
                    "Embed metrics depicting the cohort scales you managed in prior internships."
                )
            }
            "AI Engineer" -> {
                score = 72
                matched = listOf("Python", "PyTorch", "Basic NLP", "Linear Algebra")
                missing = listOf("LLM RAG Vector Databases", "LangChain architectures", "Quantization techniques", "Docker deployment")
                weaknesses = listOf("Lack of pipeline orchestration models", "No metrics measuring custom training efficiency")
                suggestions = listOf(
                    "Incorporate technical terms: cosine similarity, Pinecone, or prompt chunk routing.",
                    "Highlight API endpoints configured to handle LLM completions recursively."
                )
            }
            else -> {
                score = 80
                matched = listOf("Product Management", "Wireframing", "Figma", "User Interviews")
                missing = listOf("A/B Significance Testing", "Jira Sprint planning", "Amplitude Analytics", "API integrations")
                weaknesses = listOf("No numeric conversion or cohort user growth indicators", "Vague agile framework milestones")
                suggestions = listOf(
                    "Clarify user stories created, emphasizing actual sprint outcomes.",
                    "Include conversion analytics and funnel optimizations built under Figma blueprints."
                )
            }
        }

        val jsonResponse = JSONObject().apply {
            put("atsScore", score)
            put("matchedSkills", JSONArray(matched))
            put("missingSkills", JSONArray(missing))
            put("strengths", JSONArray(listOf("Elegant layout hierarchy", "Solid programming foundations")))
            put("weaknesses", JSONArray(weaknesses))
            put("suggestions", JSONArray(suggestions))
        }

        return okResponse(jsonResponse.toString(), Request.Builder().url("https://api.careeronix.ai/api/resume/upload").build())
    }

    private fun handleInterviewSubmit(request: Request): Response {
        val bodyStr = request.body?.let { bodyToString(it) } ?: "{}"
        val entry = JSONObject(bodyStr)
        val role = entry.optString("role", "Frontend Developer")
        val question = entry.optString("question", "What are React custom hooks?")
        val answer = entry.optString("answer", "")

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!apiKey.isNullOrEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    You are an expert tech hiring manager assessing candidates for: '$role'.
                    Evaluate this candidate's interview answer:
                    Question: "$question"
                    Candidate's Answer: "$answer"

                    Rate them fairly based on content quality, correctness, and professional language.
                    Output a detailed evaluation formatted strictly as valid JSON.
                    The JSON model schema must strictly contain these keys:
                    {
                      "overallScore": Integer (between 40 and 100),
                      "confidenceScore": Integer (between 40 and 100),
                      "communicationScore": Integer (between 40 and 100),
                      "technicalScore": Integer (between 40 and 100),
                      "feedback": String (a constructive 2-3 sentence technical critique)
                    }
                    Return ONLY the valid raw JSON object.
                """.trimIndent()

                val geminiJson = performGeminiRequest(apiKey, prompt)
                if (geminiJson != null) return okResponse(geminiJson, request)
            } catch (e: Exception) {
                Log.e("MockInterceptor", "Gemini Real-time Interview answer assessment failed", e)
            }
        }

        // --- RULE-BASED ROBUST LOCAL INTERVIEW FALLBACK ---
        val length = answer.trim().length
        val score: Int
        val comms: Int
        val confidence: Int
        val tech: Int
        val feedback: String

        if (length < 15) {
            score = 45
            comms = 50
            confidence = 40
            tech = 42
            feedback = "The response is too brief. Try to outline technical points and structure using the STAR framework."
        } else {
            score = 75
            comms = 80
            confidence = 78
            tech = 76
            feedback = "Solid effort. You correctly referenced basic principles. Elevate your score further by giving quick architectural or performance scale examples."
        }

        val jsonResponse = JSONObject().apply {
            put("overallScore", score)
            put("confidenceScore", confidence)
            put("communicationScore", comms)
            put("technicalScore", tech)
            put("feedback", feedback)
        }

        return okResponse(jsonResponse.toString(), request)
    }

    private fun performGeminiRequest(apiKey: String, prompt: String): String? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val contentsArray = JSONArray().apply {
            put(JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", prompt)
                    })
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
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("MockInterceptor", "Gemini API HTTP Error: ${response.code} ${response.message}")
                return null
            }
            val respStr = response.body?.string() ?: return null
            val respJson = JSONObject(respStr)
            val candidates = respJson.optJSONArray("candidates") ?: return null
            if (candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content") ?: return null
                val parts = content.optJSONArray("parts") ?: return null
                if (parts.length() > 0) {
                    val firstPart = parts.getJSONObject(0)
                    return firstPart.optString("text")?.trim()
                }
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

// --- SINGLETON RETROFIT CLIENT MANAGER ---

object CareeronixRetrofitClient {
    private var instance: CareeronixApiService? = null

    fun getClient(context: Context): CareeronixApiService {
        if (instance == null) {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(AuthTokenInterceptor(context))
                .addInterceptor(MockInterceptor(context))
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.careeronix.ai/")
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            instance = retrofit.create(CareeronixApiService::class.java)
        }
        return instance!!
    }
}
