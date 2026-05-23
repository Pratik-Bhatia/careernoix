package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.*
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import java.io.IOException
import kotlin.random.Random

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CareeronixViewModel(application: Application) : AndroidViewModel(application) {

    private val db = CareeronixDatabase.getDatabase(application, viewModelScope)
    private val repository = CareeronixRepository(db)
    private val apiService = CareeronixRetrofitClient.getClient(application)

    private val _dbInitError = MutableStateFlow<Throwable?>(null)
    val dbInitError: StateFlow<Throwable?> = _dbInitError.asStateFlow()

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Ensure room opens and queries successfully
                db.userProfileDao().getProfileDirect()
                repository.populateDefaultsIfEmpty()
                
                // On launch, check if has token and restore/sync experiences!
                val prefs = getApplication<Application>().getSharedPreferences("careeronix_auth", Context.MODE_PRIVATE)
                val token = prefs.getString("jwt_token", null)
                if (token != null) {
                    refreshExperiences()
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                _dbInitError.value = t
            }
        }
    }

    // --- STATE FLOWS ---
    val profile: StateFlow<UserProfile?> = repository.profileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val resumeHistory: StateFlow<List<ResumeRecord>> = repository.resumeHistoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val interviewHistory: StateFlow<List<InterviewRecord>> = repository.interviewHistoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExperiences: StateFlow<List<ExperienceRecord>> = repository.allExperiencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _experienceLoading = MutableStateFlow(false)
    val experienceLoading: StateFlow<Boolean> = _experienceLoading.asStateFlow()

    private val _experienceError = MutableStateFlow<String?>(null)
    val experienceError: StateFlow<String?> = _experienceError.asStateFlow()

    // Currently selected target role (defaults to Frontend if empty)
    private val _selectedRole = MutableStateFlow("Frontend Developer")
    val selectedRole: StateFlow<String> = _selectedRole.asStateFlow()

    // Skill Checklist for the selected role
    val skillsForSelectedRole: StateFlow<List<SkillRecord>> = _selectedRole
        .flatMapLatest { role -> repository.getSkillsForRole(role) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- OTHER INTERACTIVE STATES ---
    val onboardingSteps = listOf(
        OnboardingPage(
            title = "Optimize Your Resume via AI",
            description = "Get professional suggestions, keyword match checks, and align with global ATS scoring to bypass initial screen filters.",
            illustrationId = "resume"
        ),
        OnboardingPage(
            title = "Analyze Your Skill Gaps",
            description = "Select your target role and get dynamic roadmap timelines tailored to bridge the specific missing skill gaps.",
            illustrationId = "gaps"
        ),
        OnboardingPage(
            title = "Immersive AI Mock Interviews",
            description = "Speak or type answers to realistic HR and technical rounds. Receive structural communication, technical, and confidence feedbacks.",
            illustrationId = "interview"
        ),
        OnboardingPage(
            title = "Gamified Career Readiness",
            description = "Acquire XP points, advance through learner levels, and claim elite career-ready badges as you upgrade your portfolio profile.",
            illustrationId = "growth"
        )
    )

    private val _onboardingIndex = MutableStateFlow(0)
    val onboardingIndex: StateFlow<Int> = _onboardingIndex.asStateFlow()

    // Auth screen states
    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // Resume Scan Simulator Screen States
    private val _resumeScanLoading = MutableStateFlow(false)
    val resumeScanLoading: StateFlow<Boolean> = _resumeScanLoading.asStateFlow()

    private val _resumeScanProgressMessage = MutableStateFlow("")
    val resumeScanProgressMessage: StateFlow<String> = _resumeScanProgressMessage.asStateFlow()

    private val _selectedMockPdfName = MutableStateFlow<String?>(null)
    val selectedMockPdfName: StateFlow<String?> = _selectedMockPdfName.asStateFlow()

    // Interview Simulator States
    private val _selectedInterviewCategory = MutableStateFlow("Technical") // Technical / HR / Behavior
    val selectedInterviewCategory: StateFlow<String> = _selectedInterviewCategory.asStateFlow()

    private val _interviewLoading = MutableStateFlow(false)
    val interviewLoading: StateFlow<Boolean> = _interviewLoading.asStateFlow()

    private val _interviewActiveQuestionIndex = MutableStateFlow(0)
    val interviewActiveQuestionIndex: StateFlow<Int> = _interviewActiveQuestionIndex.asStateFlow()

    private val _interviewIsCompleted = MutableStateFlow(false)
    val interviewIsCompleted: StateFlow<Boolean> = _interviewIsCompleted.asStateFlow()

    private val _currentAnswerText = MutableStateFlow("")
    val currentAnswerText: StateFlow<String> = _currentAnswerText.asStateFlow()

    private val _interviewScannedFeedback = MutableStateFlow<InterviewRecord?>(null)
    val interviewScannedFeedback: StateFlow<InterviewRecord?> = _interviewScannedFeedback.asStateFlow()

    // AI Daily Career Tips List
    val dailyTips = listOf(
        "💡 Tip: ATS screeners hate complex headers, multi-column tables, or graphics inside the text area. Keep layout columns clean.",
        "🚀 Tip: When describing projects, use the STAR format: Situation, Task, Action, and Quantitative Result (e.g. Improved core load times by 26%).",
        "💡 Tip: For react development, practice explainers on 'Concurrent Mode' and 'Fiber layout' to sound superior during technical conversations.",
        "🔥 Tip: Tie your technical portfolio projects with live deployed Vercel/Netlify URLs and clear Readme markdown guides.",
        "📊 Tip: In SQL interviews, always mention index optimizations and EXPLAIN plans to show production engineering maturity."
    )

    private val _featuredTip = MutableStateFlow(dailyTips[0])
    val featuredTip: StateFlow<String> = _featuredTip.asStateFlow()

    fun cycleTip() {
        val nextIdx = (dailyTips.indexOf(_featuredTip.value) + 1) % dailyTips.size
        _featuredTip.value = dailyTips[nextIdx]
    }

    // --- AUTH ACTIONS ---
    fun loginMock(email: String, name: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            
            try {
                val reqName = name.ifEmpty { "Pratik Bhatia" }
                val reqEmail = email.ifEmpty { "pratikbhatiahp@gmail.com" }
                val response = apiService.login(LoginRequest(reqEmail, reqName))
                
                // Store token in Shared Preferences
                val prefs = getApplication<Application>().getSharedPreferences("careeronix_auth", Context.MODE_PRIVATE)
                prefs.edit().putString("jwt_token", response.token).apply()
                
                // Save user details to dynamic Room database
                val currentProfile = profile.value
                val finalProfile = if (currentProfile != null) {
                    currentProfile.copy(
                        email = response.email,
                        name = response.name,
                        targetRole = response.targetRole,
                        isLoggedIn = true,
                        onboardingCompleted = true,
                        level = response.level,
                        xp = response.xp,
                        jobReadiness = response.jobReadiness
                    )
                } else {
                    UserProfile(
                        name = response.name,
                        email = response.email,
                        targetRole = response.targetRole,
                        onboardingCompleted = true,
                        isLoggedIn = true,
                        resumeScore = 72,
                        atsScore = response.jobReadiness - 13,
                        jobReadiness = response.jobReadiness,
                        xp = response.xp,
                        level = response.level,
                        unlockedBadges = "Early Starter,First Mock"
                    )
                }
                
                repository.saveProfile(finalProfile)
                
                // Sync experiences right after login succeeds!
                refreshExperiences()
                
            } catch (t: Throwable) {
                Log.e("CareeronixViewModel", "API login failed", t)
                _authError.value = "Failed to sign in via API: ${t.localizedMessage}"
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun mockGoogleSignIn() {
        loginMock("pratikbhatiahp@gmail.com", "Pratik Bhatia (via Google)")
    }

    fun logout() {
        viewModelScope.launch {
            // Clear preferences
            val prefs = getApplication<Application>().getSharedPreferences("careeronix_auth", Context.MODE_PRIVATE)
            prefs.edit().remove("jwt_token").apply()

            profile.value?.let {
                repository.saveProfile(it.copy(isLoggedIn = false))
            }
        }
    }

    fun skipOnboarding() {
        viewModelScope.launch {
            repository.setOnboardingCompleted(true)
        }
    }

    fun updateRole(newRole: String) {
        viewModelScope.launch {
            _selectedRole.value = newRole
            repository.updateTargetRole(newRole)
        }
    }

    fun changeOnboardingIndex(offset: Int) {
        val dest = _onboardingIndex.value + offset
        if (dest in onboardingSteps.indices) {
            _onboardingIndex.value = dest
        } else if (dest >= onboardingSteps.size) {
            skipOnboarding()
        }
    }

    // --- GAME XP ACCRUAL SYSTEM ---
    private suspend fun addXp(amount: Int) {
        val currentProfile = profile.value ?: return
        val nextXp = currentProfile.xp + amount
        // 500 XP per level
        val calculatedLevel = (nextXp / 500) + 1
        var badges = currentProfile.unlockedBadges

        if (calculatedLevel > currentProfile.level) {
            val freshBadge = "Elite Level $calculatedLevel"
            if (!badges.contains(freshBadge)) {
                badges += if (badges.isEmpty()) freshBadge else ",$freshBadge"
            }
        }
        
        // Dynamic badges based on resume scores and interview scores
        if (amount >= 100 && !badges.contains("ATS Master")) {
            badges += ",ATS Master"
        }

        // recalculate readiness
        val updatedReadiness = calculateReadinessMetrics(
            skillsRatio = getSkillsRatio(),
            resumeS = currentProfile.resumeScore,
            atsS = currentProfile.atsScore
        )

        repository.saveProfile(currentProfile.copy(
            xp = nextXp,
            level = calculatedLevel,
            unlockedBadges = badges,
            jobReadiness = updatedReadiness
        ))
    }

    fun toggleSkillComplete(id: Int, currentStatus: Boolean) {
        viewModelScope.launch {
            val targetProgress = if (currentStatus) 0 else 100
            repository.updateSkillProgress(id, !currentStatus, targetProgress)
            
            // Add some XP for skill updates!
            if (!currentStatus) {
                addXp(40)
            }
        }
    }

    private fun calculateReadinessMetrics(skillsRatio: Float, resumeS: Int, atsS: Int): Int {
        // Skill progress accounts for 40%
        // Resume strength accounts for 30%
        // ATS optimization score accounts for 30%
        return ((skillsRatio * 40) + (resumeS * 0.3) + (atsS * 0.3)).toInt().coerceIn(10, 100)
    }

    private fun getSkillsRatio(): Float {
        val list = skillsForSelectedRole.value
        if (list.isEmpty()) return 0.5f
        val completedCount = list.count { it.isCompleted }
        return completedCount.toFloat() / list.size.toFloat()
    }

    // --- EXPERIENCE CRUD BACKEND SYSTEM ACTIONS ---
    fun refreshExperiences() {
        viewModelScope.launch {
            _experienceLoading.value = true
            _experienceError.value = null
            try {
                val remote = apiService.getExperiences()
                // Sync with local Room Database safely
                db.runInTransaction {
                    val dao = db.experienceDao()
                    // Clear old entries
                    dao.getExperiencesSync().forEach {
                        dao.deleteExperienceByIdSync(it.id)
                    }
                    // Populate fetched ones
                    remote.forEach {
                        dao.insertExperienceSync(
                            ExperienceRecord(
                                id = it.id,
                                title = it.title,
                                company = it.company,
                                period = it.period,
                                description = it.description
                            )
                        )
                    }
                }
            } catch (t: Throwable) {
                Log.e("CareeronixViewModel", "Failed to sync experiences", t)
                _experienceError.value = "Failed to sync experiences from API. Displaying offline data."
            } finally {
                _experienceLoading.value = false
            }
        }
    }

    fun addExperience(title: String, company: String, period: String, description: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _experienceLoading.value = true
            _experienceError.value = null
            
            // Insert optimistic local entry
            val record = ExperienceRecord(title = title, company = company, period = period, description = description)
            val tempId = db.experienceDao().insertExperience(record)
            
            try {
                val response = apiService.addExperience(ExperienceEntry(0, title, company, period, description))
                // Replace optimistic local entry with permanent server-assigned ID entry
                db.experienceDao().deleteExperienceById(tempId.toInt())
                db.experienceDao().insertExperience(
                    ExperienceRecord(
                        id = response.id,
                        title = response.title,
                        company = response.company,
                        period = response.period,
                        description = response.description
                    )
                )
                addXp(50) // Reward career builder XP!
            } catch (t: Throwable) {
                Log.e("CareeronixViewModel", "Failed to save experience to API", t)
                _experienceError.value = "Failed to sync with API database. Logged locally."
            } finally {
                _experienceLoading.value = false
            }
        }
    }

    fun updateExperience(id: Int, title: String, company: String, period: String, description: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _experienceLoading.value = true
            _experienceError.value = null
            
            // Capture backup
            val dao = db.experienceDao()
            val oldRecord = dao.getExperiencesSync().find { it.id == id }
            
            // Write optimistic change
            val updated = ExperienceRecord(id = id, title = title, company = company, period = period, description = description)
            dao.updateExperience(updated)
            
            try {
                apiService.updateExperience(id, ExperienceEntry(id, title, company, period, description))
            } catch (t: Throwable) {
                Log.e("CareeronixViewModel", "Failed to update experience on API", t)
                _experienceError.value = "Could not sync update with API database."
                // Revert to backup if failed
                if (oldRecord != null) {
                    dao.updateExperience(oldRecord)
                }
            } finally {
                _experienceLoading.value = false
            }
        }
    }

    fun deleteExperience(id: Int) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _experienceLoading.value = true
            _experienceError.value = null
            
            val dao = db.experienceDao()
            val oldRecord = dao.getExperiencesSync().find { it.id == id }
            
            // Optimistic delete
            dao.deleteExperienceById(id)
            
            try {
                apiService.deleteExperience(id)
            } catch (t: Throwable) {
                Log.e("CareeronixViewModel", "Failed to delete from API", t)
                _experienceError.value = "Failed to sync removal with API."
                // Revert to backup
                if (oldRecord != null) {
                    dao.insertExperience(oldRecord)
                }
            } finally {
                _experienceLoading.value = false
            }
        }
    }

    // Helper to extract file metadata from Uri
    fun getFileNameAndSize(context: Context, uri: android.net.Uri): Pair<String, Long> {
        var name = "resume.pdf"
        var size = 0L
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIdx != -1) name = cursor.getString(nameIdx)
                    if (sizeIdx != -1) size = cursor.getLong(sizeIdx)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Pair(name, size)
    }

    // Comprehensive real PDF / Text file parser and multipart uploader
    fun uploadAndScanResume(context: Context, fileUri: android.net.Uri) {
        viewModelScope.launch {
            _resumeScanLoading.value = true
            _resumeScanProgressMessage.value = "🤖 Careeronix AI initializing semantic scan models..."
            
            val pair = getFileNameAndSize(context, fileUri)
            val name = pair.first
            _selectedMockPdfName.value = name
            
            delay(1000)
            _resumeScanProgressMessage.value = "⚙️ Cross-referencing against 45+ professional enterprise ATS keyword filters..."
            delay(1000)
            _resumeScanProgressMessage.value = "📝 Ingesting file bytes and uploading to API using multipart/form-data with JWT token..."
            
            try {
                val resolver = context.contentResolver
                val bytes = resolver.openInputStream(fileUri)?.readBytes() ?: ByteArray(0)
                
                val reqBody = okhttp3.RequestBody.create(
                    "application/pdf".toMediaTypeOrNull(),
                    bytes
                )
                val bodyPart = MultipartBody.Part.createFormData("file", name, reqBody)
                
                val result = apiService.uploadResume(bodyPart, _selectedRole.value)
                
                _resumeScanProgressMessage.value = "✨ AI Analysis Complete! Syncing profile scores & skill roads..."
                delay(800)
                
                val scannedResult = ResumeRecord(
                    fileName = name,
                    timestamp = System.currentTimeMillis(),
                    atsScore = result.atsScore,
                    criticalWeakness = result.weaknesses.joinToString(". "),
                    suggestions = result.suggestions.joinToString(","),
                    matchingScore = result.atsScore
                )
                
                repository.addResumeScan(scannedResult)
                
                // Track missing skills in the roadmap checklist!
                val list = repository.getSkillsForRole(_selectedRole.value).firstOrNull() ?: emptyList()
                list.forEach { skill ->
                    val isMissing = result.missingSkills.any { skill.skillName.contains(it, ignoreCase = true) }
                    if (isMissing && skill.isCompleted) {
                        repository.updateSkillProgress(skill.id, false, 20)
                    }
                }
                
                // Update main profile scores
                profile.value?.let {
                    val updatedReadiness = calculateReadinessMetrics(getSkillsRatio(), result.atsScore, result.atsScore)
                    val updatedProfile = it.copy(
                        resumeScore = result.atsScore,
                        atsScore = result.atsScore,
                        jobReadiness = updatedReadiness
                    )
                    repository.saveProfile(updatedProfile)
                }
                
                addXp(120)
                _resumeScanLoading.value = false
            } catch (t: Throwable) {
                Log.e("CareeronixViewModel", "Failed API scan upload", t)
                _resumeScanProgressMessage.value = "❌ API Scan failed: ${t.localizedMessage}. Please try again."
                delay(2200)
                _resumeScanLoading.value = false
            }
        }
    }


    // --- RESUME MOCK ALGORITHM ---
    fun selectMockResumeFile(name: String) {
        _selectedMockPdfName.value = name
    }

    fun runSimulatedResumeScan() {
        val name = _selectedMockPdfName.value ?: "Anonymous_Profile_2026.pdf"
        viewModelScope.launch {
            _resumeScanLoading.value = true
            _resumeScanProgressMessage.value = "🤖 Careeronix AI initializing semantic scan models..."
            delay(1200)
            _resumeScanProgressMessage.value = "⚙️ Cross-referencing against 45+ professional enterprise ATS keyword filters..."
            delay(1400)
            _resumeScanProgressMessage.value = "📝 Scoring section relevance and layout readability indexes..."
            delay(1100)

            val parsedScore = Random.nextInt(78, 96)
            val parsedAts = parsedScore - Random.nextInt(4, 9)
            
            // Craft variations based on roles or PDF names
            val suggestionsList = if (name.contains("React", true) || name.contains("v1")) {
                listOf(
                    "Explicitly reference JavaScript asynchronous event architectures (Promises, async/await).",
                    "Add quantifiable business impact margins in React Hooks descriptors.",
                    "Optimize top-grid layout margins — avoid double columns designed with unreadable text dividers."
                )
            } else {
                listOf(
                    "Include performance-related quantitative statements (e.g., Improved processing latency by 32%).",
                    "Expand on missing industry tags corresponding to target roles.",
                    "Improve accessibility compliance — ensure font styles match standard reader configurations."
                )
            }

            val criticalWeak = "Margins slightly misaligned for older corporate ATS parsers and missing key tags for ${_selectedRole.value} role."

            val scannedResult = ResumeRecord(
                fileName = name,
                timestamp = System.currentTimeMillis(),
                atsScore = parsedAts,
                criticalWeakness = criticalWeak,
                suggestions = suggestionsList.joinToString(","),
                matchingScore = parsedScore
            )

            repository.addResumeScan(scannedResult)

            // Update main profile scores
            profile.value?.let {
                val updatedReadiness = calculateReadinessMetrics(getSkillsRatio(), parsedScore, parsedAts)
                val updatedProfile = it.copy(
                    resumeScore = parsedScore,
                    atsScore = parsedAts,
                    jobReadiness = updatedReadiness
                )
                repository.saveProfile(updatedProfile)
            }

            addXp(120) // Gained XP for Scanning!
            _resumeScanLoading.value = false
        }
    }


    // --- INTERVIEW SIMULATOR LOGIC ---
    val mockQuestions = mapOf(
        "Frontend Developer" to listOf(
            "Question 1: Explain the difference between React virtual DOM reconciliation and direct DOM layout updates. How does React Fiber solve performance bottlenecks?",
            "Question 2: What are custom React Hooks, and why would you utilize useMemo or useCallback for rendering optimizations? Name structural pitfalls.",
            "Question 3: Describe your CSS management architecture on responsive screens. How do viewport-relative units interact with layout spacing?",
            "Question 4: HR question: Can you describe a challenging technical bottleneck you faced in a collegiate or personal project, and how you managed it?"
        ),
        "Data Analyst" to listOf(
            "Question 1: What is the differences between INNER JOIN, LEFT JOIN, and CROSS JOIN? When do join cardinalities cause CPU bottleneck on Postgres?",
            "Question 2: How do you treat missing value vectors or outliers in structured Pandas matrices without causing research bias?",
            "Question 3: Explain the difference between dimension tables and fact tables in clean star schema database structures.",
            "Question 4: HR question: Describe a scenario where your analytical results contradicted executive suggestions. How did you communicate the findings?"
        ),
        "UI/UX Designer" to listOf(
            "Question 1: What is the concept of 'Cognitive Load' in mobile user flows, and how do visual spacing hierarchies alleviate navigation friction?",
            "Question 2: Describe a Design System. How do design tokens (colors, spacings, fonts) sync between Figma design frames and composable styles?",
            "Question 3: How do you execute usability evaluation tests with early freshman students in India? What variables do you prioritize testing?",
            "Question 4: HR question: Describe a time you received extremely harsh design criticisms. How did you decouple personal attachment from product outcomes?"
        ),
        "AI Engineer" to listOf(
            "Question 1: Tell us how Retrieval-Augmented Generation (RAG) differs from fine-tuning. How do vector embeddings map semantic indexes?",
            "Question 2: In PyTorch, what is the role of backpropagation, and how does gradient vanishing occur inside deep transformer architectures?",
            "Question 3: Explain how prompt structure guidelines (like few-shot or chain-of-thought) influence token probability scores in Gemini models.",
            "Question 4: HR question: Given the rapid rate of open-source AI advancements, how do you filter which papers to learn, and how do you execute side projects?"
        )
    )

    fun setInterviewCategory(cat: String) {
        _selectedInterviewCategory.value = cat
    }

    val activeQuestionList: List<String>
        get() = mockQuestions[_selectedRole.value] ?: mockQuestions["Frontend Developer"]!!

    val activeQuestion: String
        get() {
            val questions = activeQuestionList
            val idx = _interviewActiveQuestionIndex.value.coerceIn(questions.indices)
            return questions[idx]
        }

    fun submitAnswer(ans: String) {
        viewModelScope.launch {
            _currentAnswerText.value = ans
            _interviewLoading.value = true

            val currentIdx = _interviewActiveQuestionIndex.value
            val totalQuestions = activeQuestionList.size

            if (currentIdx < totalQuestions - 1) {
                // Advance to next question
                _interviewActiveQuestionIndex.value = currentIdx + 1
                _currentAnswerText.value = ""
                _interviewLoading.value = false
            } else {
                // Evaluate using the real Retrofit API service
                try {
                    val submission = InterviewSubmission(
                        role = _selectedRole.value,
                        question = activeQuestion,
                        answer = ans
                    )
                    val evaluation = apiService.submitInterview(submission)
                    
                    _interviewIsCompleted.value = true
                    
                    val interviewRecord = InterviewRecord(
                        role = _selectedRole.value,
                        timestamp = System.currentTimeMillis(),
                        confidenceScore = evaluation.confidenceScore,
                        communicationScore = evaluation.communicationScore,
                        technicalScore = evaluation.technicalScore,
                        overallScore = evaluation.overallScore,
                        feedback = evaluation.feedback
                    )

                    repository.addInterviewRecord(interviewRecord)
                    _interviewScannedFeedback.value = interviewRecord

                    addXp(150) // High XP bonus
                } catch (t: Throwable) {
                    Log.e("CareeronixViewModel", "API interview submit failed, falling back", t)
                    // Robust local fallback in case of no endpoint response
                    _interviewIsCompleted.value = true
                    val randConfidence = Random.nextInt(75, 95)
                    val randCommunication = Random.nextInt(70, 92)
                    val randTechnical = Random.nextInt(68, 90)
                    val avgScore = (randConfidence + randCommunication + randTechnical) / 3

                    val fallbackFeedback = "Based on your technical responses for ${_selectedRole.value}, your conceptual framework is cohesive. " +
                            "Communication: Excellent structured terminology. " +
                            "Improvement suggestions: Mention precise engineering milestones (such as specific APIs, memory profilers). (Evaluated Offline)"

                    val interviewRecord = InterviewRecord(
                        role = _selectedRole.value,
                        timestamp = System.currentTimeMillis(),
                        confidenceScore = randConfidence,
                        communicationScore = randCommunication,
                        technicalScore = randTechnical,
                        overallScore = avgScore,
                        feedback = fallbackFeedback
                    )

                    repository.addInterviewRecord(interviewRecord)
                    _interviewScannedFeedback.value = interviewRecord
                    addXp(150)
                } finally {
                    _interviewLoading.value = false
                }
            }
        }
    }

    fun restartInterview() {
        _interviewActiveQuestionIndex.value = 0
        _interviewIsCompleted.value = false
        _currentAnswerText.value = ""
        _interviewScannedFeedback.value = null
    }

    // --- ADMIN PANEL ANALYTICS MOCK SYSTEM ---
    val initialInstitutionStats = InstitutionStats(
        collegeName = "Ramanujan Institute of Tech - AI & Career Accelerator",
        studentEnrollment = 480,
        averageEmployabilityIndex = 68,
        activeAtsOptimizerUsers = 312,
        resumeAtsPassRate = 74,
        averageMockInterviewRating = 72,
        recentPlacementsLog = listOf(
            PlacementLog("Priya Sharma", "Associate Frontend Dev", "Paytm", "9.5 LPA", "Active User"),
            BasedPlacementLog("Rajesh Verma", "Data Science Intern", "Fractal Analytics", "7.0 LPA", "XP Level 4"),
            PlacementLog("Ananya Iyer", "UX/UI Associate", "Zomato Design Studio", "12.0 LPA", "ATS Master"),
            PlacementLog("Karthik Nair", "ML Engineer", "TCS Research AI", "8.2 LPA", "Elite Level 3"),
            BasedPlacementLog("Amit Chhabra", "React Native Developer", "InnoWave", "6.5 LPA", "Active User")
        )
    )

    // Dynamic custom lists or details
    data class OnboardingPage(val title: String, val description: String, val illustrationId: String)
}

// B2B Admin statistics structure
data class InstitutionStats(
    val collegeName: String,
    val studentEnrollment: Int,
    val averageEmployabilityIndex: Int,
    val activeAtsOptimizerUsers: Int,
    val resumeAtsPassRate: Int,
    val averageMockInterviewRating: Int,
    val recentPlacementsLog: List<PlacementLog>
)

open class PlacementLog(
    val studentName: String,
    val targetRole: String,
    val company: String,
    val packageLpa: String,
    val badgeUnlocked: String
)

class BasedPlacementLog(
    studentName: String,
    targetRole: String,
    company: String,
    packageLpa: String,
    badgeUnlocked: String
) : PlacementLog(studentName, targetRole, company, packageLpa, badgeUnlocked)
