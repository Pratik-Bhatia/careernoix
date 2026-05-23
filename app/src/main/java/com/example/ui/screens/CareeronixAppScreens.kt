package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.viewmodel.CareeronixViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- CENTRAL NAVIGATION ORCHESTRATOR ---

@Composable
fun CareeronixMainApp(viewModel: CareeronixViewModel) {
    val profileState by viewModel.profile.collectAsState()
    var currentScreen by remember { mutableStateOf("splash") }

    // Navigation trigger on first-load or auth state changes
    LaunchedEffect(profileState) {
        if (currentScreen == "splash") {
            delay(2200) // Beautiful splash showing
            val profile = profileState
            if (profile == null || !profile.onboardingCompleted) {
                currentScreen = "onboarding"
            } else if (!profile.isLoggedIn) {
                currentScreen = "auth"
            } else {
                currentScreen = "dashboard_parent"
            }
        } else {
            val profile = profileState
            if (profile != null && profile.onboardingCompleted && profile.isLoggedIn && currentScreen != "dashboard_parent") {
                currentScreen = "dashboard_parent"
            } else if ((profile == null || !profile.isLoggedIn) && currentScreen == "dashboard_parent") {
                currentScreen = "auth"
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (currentScreen) {
            "splash" -> WelcomeSplashScreen()
            "onboarding" -> OnboardingFlow(
                viewModel = viewModel,
                onFinished = {
                    currentScreen = "auth"
                    viewModel.skipOnboarding()
                }
            )
            "auth" -> AuthScreen(
                viewModel = viewModel,
                onSuccess = {
                    currentScreen = "dashboard_parent"
                }
            )
            "dashboard_parent" -> DashboardParent(viewModel = viewModel)
        }
    }
}


// --- 1. WELCOME SPLASH SCREEN ---

@Composable
fun WelcomeSplashScreen() {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF4F46E5), Color(0xFF06B6D4))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Elegant modern Careeronix Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .drawBehind {
                        drawCircle(
                            color = Color.White,
                            radius = size.minDimension / 5f,
                            center = center
                        )
                    }
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = "Careeronix Logo Icon",
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Careeronix",
                fontSize = 38.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Build Your Career with AI",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
        }

        // Subtext aligned to bottom
        Text(
            text = "Careeronix AI Ecosystem • Version 1.0",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
        )
    }
}


// --- 2. ONBOARDING FLOW ---

@Composable
fun OnboardingFlow(
    viewModel: CareeronixViewModel,
    onFinished: () -> Unit
) {
    val currentIndex by viewModel.onboardingIndex.collectAsState()
    val steps = viewModel.onboardingSteps
    val currentPage = steps[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Skip header button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onFinished,
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text(
                    text = "Skip",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Large Premium Vector Mock Illustration Card
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 16.dp)
                .size(170.dp)
                .shadow(8.dp, RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (currentPage.illustrationId) {
                    "resume" -> Icons.Default.Description
                    "gaps" -> Icons.Default.Assignment
                    "interview" -> Icons.Default.RecordVoiceOver
                    else -> Icons.Default.SportsEsports
                },
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(72.dp)
            )
        }

        Spacer(modifier = Modifier.weight(0.8f))

        // Content
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "OnboardingContent"
        ) { page ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = page.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = page.description,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1.2f))

        // Dot indicators
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            steps.forEachIndexed { idx, _ ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(height = 8.dp, width = if (idx == currentIndex) 24.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (idx == currentIndex) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Primary Control actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentIndex > 0) {
                OutlinedButton(
                    onClick = { viewModel.changeOnboardingIndex(-1) },
                    modifier = Modifier.testTag("onboarding_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back previous onboarding slide"
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(60.dp))
            }

            Button(
                onClick = {
                    if (currentIndex == steps.size - 1) {
                        onFinished()
                    } else {
                        viewModel.changeOnboardingIndex(1)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .height(50.dp)
                    .testTag("onboarding_next_button")
            ) {
                Text(
                    text = if (currentIndex == steps.size - 1) "Get Started" else "Next",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}


// --- 3. AUTHENTICATION & LOGIN ---

@Composable
fun AuthScreen(
    viewModel: CareeronixViewModel,
    onSuccess: () -> Unit
) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("pratikbhatiahp@gmail.com") }
    var name by remember { mutableStateOf("Pratik Bhatia") }
    var password by remember { mutableStateOf("******") }
    var isOtpSent by remember { mutableStateOf(false) }
    var otpValue by remember { mutableStateOf("") }

    val isLoading by viewModel.isAuthLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Branded mini logo
            Icon(
                imageVector = Icons.Default.RocketLaunch,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Careeronix",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Accelerate to employment ready status",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Switch Tab Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (!isSignUp) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { isSignUp = false }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sign In",
                        color = if (!isSignUp) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSignUp) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { isSignUp = true }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sign Up",
                        color = if (isSignUp) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isOtpSent) {
                // Name Field (For Sign Up)
                if (isSignUp) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_name_field"),
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("College or Professional Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_field"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Secure Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_field"),
                    shape = RoundedCornerShape(14.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Forgot Password?",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            isOtpSent = true
                        }
                    )
                }
            } else {
                // OTP Field
                Text(
                    text = "Simulated OTP sent to $email. Enter 4-digit code below.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = otpValue,
                    onValueChange = { if (it.length <= 4) otpValue = it },
                    label = { Text("OTP Verification Code (e.g. 1294)") },
                    leadingIcon = { Icon(Icons.Default.Sms, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_otp_field"),
                    shape = RoundedCornerShape(14.dp)
                )

                TextButton(
                    onClick = { isOtpSent = false }
                ) {
                    Text("Back to Standard Login")
                }
            }

            if (authError != null) {
                Text(
                    text = authError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Main trigger Button
            Button(
                onClick = {
                    if (isOtpSent && otpValue.isEmpty()) {
                        otpValue = "1294"
                    }
                    viewModel.loginMock(email, name)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("auth_submit_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (isOtpSent) "Verify & Access" else if (isSignUp) "Create Account" else "Log In",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "OR CONTINUE WITH",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Google Signin style Row Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clickable { viewModel.mockGoogleSignIn() }
                    .testTag("google_auth_button"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Adjust, // Custom Google asset represent
                        contentDescription = "Google Icon representation",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign in with Google",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "By continuing, you agree and consent to the fully compliant automated Careeronix privacy policies.",
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}


// --- 4. DASHBOARD PARENT WITH BOTTOM NAVIGATION BAR ---

@Composable
fun DashboardParent(viewModel: CareeronixViewModel) {
    var selectedTab by remember { mutableStateOf("home") }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = selectedTab == "home",
                    onClick = { selectedTab = "home" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard Core Home") },
                    label = { Text("Core") },
                    modifier = Modifier.testTag("nav_tab_home")
                )

                NavigationBarItem(
                    selected = selectedTab == "resume",
                    onClick = { selectedTab = "resume" },
                    icon = { Icon(Icons.Default.Description, contentDescription = "AI Resume Optimization analyses") },
                    label = { Text("Resume") },
                    modifier = Modifier.testTag("nav_tab_resume")
                )

                NavigationBarItem(
                    selected = selectedTab == "interview",
                    onClick = { selectedTab = "interview" },
                    icon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = "AI Mock Interview preparation") },
                    label = { Text("Interview") },
                    modifier = Modifier.testTag("nav_tab_interview")
                )

                NavigationBarItem(
                    selected = selectedTab == "roadmap",
                    onClick = { selectedTab = "roadmap" },
                    icon = { Icon(Icons.Default.AltRoute, contentDescription = "Skill roadmap gap analyst") },
                    label = { Text("Skills") },
                    modifier = Modifier.testTag("nav_tab_roadmap")
                )

                NavigationBarItem(
                    selected = selectedTab == "college",
                    onClick = { selectedTab = "college" },
                    icon = { Icon(Icons.Default.School, contentDescription = "B2B Admin statistics panels") },
                    label = { Text("College") },
                    modifier = Modifier.testTag("nav_tab_college")
                )

                NavigationBarItem(
                    selected = selectedTab == "profile",
                    onClick = { selectedTab = "profile" },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Gamified Tracker progression logs") },
                    label = { Text("Growth") },
                    modifier = Modifier.testTag("nav_tab_profile")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                "home" -> HomeDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToTab = { tab -> selectedTab = tab }
                )
                "resume" -> ResumeAnalyzerScreen(viewModel = viewModel)
                "interview" -> InterviewPrepScreen(viewModel = viewModel)
                "roadmap" -> SkillRoadmapScreen(viewModel = viewModel)
                "college" -> CollegeDashboardScreen(viewModel = viewModel)
                "profile" -> GamifiedGrowthTracker(viewModel = viewModel)
            }
        }
    }
}


// --- 5. DETAILED SCREEN: HOME DASHBOARD ---

@Composable
fun HomeDashboardScreen(
    viewModel: CareeronixViewModel,
    onNavigateToTab: (String) -> Unit
) {
    val profileState by viewModel.profile.collectAsState()
    val featuredTip by viewModel.featuredTip.collectAsState()
    val skills by viewModel.skillsForSelectedRole.collectAsState()

    val completedSkills = skills.count { it.isCompleted }
    val totalSkills = skills.size.coerceAtLeast(1)

    val currentProfile = profileState ?: return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming header card
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentProfile.name.firstOrNull()?.toString()?.uppercase() ?: "P",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            text = "Welcome back,",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = currentProfile.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Level ${currentProfile.level}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Job Readiness radial-equivalent status card
        item {
            val gradientBrush = Brush.linearGradient(
                colors = listOf(Color(0xFF4F46E5), Color(0xFF3730A3))
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(32.dp), clip = false),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .background(gradientBrush)
                        .padding(24.dp)
                ) {
                    // Abstract Background Glow
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 30.dp, y = (-30).dp)
                            .background(Color.White.copy(alpha = 0.12f), CircleShape)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Job Readiness Score",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "${currentProfile.jobReadiness}",
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    lineHeight = 44.sp
                                )
                                Text(
                                    text = "%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Progress representation
                            val readinessAnim by animateFloatAsState(
                                targetValue = currentProfile.jobReadiness / 100f,
                                label = "Readiness Progress anim"
                            )
                            LinearProgressIndicator(
                                progress = { readinessAnim },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = Color(0xFF34D399), // Emerald highlight
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            val readinessTagline = when {
                                currentProfile.jobReadiness >= 80 -> "You are highly competitive! Complete 2 more modules to reach 95%."
                                currentProfile.jobReadiness >= 60 -> "Excellent step progression. Complete gaps in roadmaps to grow."
                                else -> "Initial steps needed. Scan resumes and train AI Mock rounds."
                            }

                            Text(
                                text = readinessTagline,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.White.copy(alpha = 0.9f),
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Simulated Circle Gauge design using Box
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                                Text(
                                    text = "${currentProfile.xp} XP",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Three Column Scorecard panel
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Resume Analyzer score
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToTab("resume") }
                        .shadow(2.dp, RoundedCornerShape(24.dp), clip = false),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "RESUME",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "${currentProfile.resumeScore}/100",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFEEF2FF))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Check Scan", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5))
                        }
                    }
                }

                // ATS screener optimization score
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToTab("resume") }
                        .shadow(2.dp, RoundedCornerShape(24.dp), clip = false),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFECFEFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF06B6D4),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "ATS MATCH",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "${currentProfile.atsScore}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFECFEFF))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Optimize", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF06B6D4))
                        }
                    }
                }

                // Skill roadmap completion
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToTab("roadmap") }
                        .shadow(2.dp, RoundedCornerShape(24.dp), clip = false),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFECFDF5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AltRoute,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "ROADMAP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "$completedSkills/$totalSkills",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFECFDF5))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Resume Gap", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                    }
                }
            }
        }

        // Daily Career Tip interactive widget
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(24.dp), clip = false),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DAILY CAREER INSIGHT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Cycle tips button
                        Text(
                            text = "Next Tip ➔",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { viewModel.cycleTip() }
                                .testTag("cycle_tip_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = featuredTip,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Recommended growth actions based on user gaps
        item {
            Column {
                Text(
                    text = "Recommended Gaps to Bridge",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Recommendation 1: Resume Optimizer
                RecommendationRow(
                    icon = Icons.Default.Description,
                    title = "Improve CV ATS compatibility score",
                    description = "We found 3 layout issues and missing tags for ${currentProfile.targetRole}.",
                    actionLabel = "Run Scan",
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClickAction = { onNavigateToTab("resume") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Recommendation 2: Interview simulation
                RecommendationRow(
                    icon = Icons.Default.Mic,
                    title = "Perform Technical Mock round",
                    description = "Latest technical assessment showed gaps in react ecosystem questions.",
                    actionLabel = "Start Practicing",
                    iconColor = Color(0xFF06B6D4),
                    onClickAction = { onNavigateToTab("interview") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Recommendation 3: Learning roadmap
                RecommendationRow(
                    icon = Icons.Default.Book,
                    title = "Complete 'React component hooks' module",
                    description = "Key required skill in selected target role stack.",
                    actionLabel = "Open Roadmap",
                    iconColor = Color(0xFF10B981),
                    onClickAction = { onNavigateToTab("roadmap") }
                )
            }
        }
    }
}

@Composable
fun RecommendationRow(
    icon: ImageVector,
    title: String,
    description: String,
    actionLabel: String,
    iconColor: Color,
    onClickAction: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp), clip = false)
            .clickable { onClickAction() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Clean action indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = actionLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}


// --- 6. AI RESUME ANALYZER SCREEN ---

@Composable
fun ResumeAnalyzerScreen(viewModel: CareeronixViewModel) {
    val resumeState by viewModel.resumeHistory.collectAsState()
    val isScanning by viewModel.resumeScanLoading.collectAsState()
    val scanMsg by viewModel.resumeScanProgressMessage.collectAsState()
    val selectedFile by viewModel.selectedMockPdfName.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val filePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.uploadAndScanResume(context, uri)
        }
    }

    val sampleFiles = listOf(
        "Pratik_Bhatia_Resume_ES6_v2.pdf",
        "Pratik_Frontend_Draft_2026.docx",
        "Standard_Tier3_GradCV.pdf"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "AI Resume Analyzer",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Bypass applicant trackers with machine-grade evaluations",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Upload visual simulator area
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = BorderStroke(2.dp, Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(54.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (selectedFile != null) "Selected: $selectedFile" else "Select a CV file to evaluate",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Real File picker button
                    Button(
                        onClick = { filePickerLauncher.launch("application/pdf") },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("custom_pdf_picker"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick & Upload custom PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "— OR SELECT PRE-BUILT SAMPLES —",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Chip selectors to simulate different files
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        sampleFiles.forEach { file ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedFile == file) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                    .clickable { viewModel.selectMockResumeFile(file) }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = file.substringBefore("."),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedFile == file) Color.White else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.runSimulatedResumeScan() },
                        enabled = !isScanning && selectedFile != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("resume_scan_trigger"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("AI Optimal Scan Resume", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Processing simulator text
        if (isScanning) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = scanMsg,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Print Latest Assessment findings
        if (resumeState.isNotEmpty()) {
            val latest = resumeState.first()

            item {
                Text(
                    text = "Optimal ATS Evaluation Metrics",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = latest.fileName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Scanned: Just now",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${latest.atsScore}% ATS Score",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        // Critical Gaps
                        Row {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Critical Gaps Identifiers",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Text(
                            text = latest.criticalWeakness,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        // Suggestions
                        Row {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI Improvement Roadmap Suggestions",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        val bulletList = latest.suggestions.split(",")
                        bulletList.forEach { sugg ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("•  ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    text = sugg,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- 7. SKILL GAP ANALYSIS & ROADMAP SCREEN ---

@Composable
fun SkillRoadmapScreen(viewModel: CareeronixViewModel) {
    val selectedRole by viewModel.selectedRole.collectAsState()
    val skillsState by viewModel.skillsForSelectedRole.collectAsState()

    val options = listOf(
        "Frontend Developer",
        "Data Analyst",
        "UI/UX Designer",
        "AI Engineer"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Target Role Skill Gaps",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Identify what elements your targets require next",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Horizontal Role Picker Chips
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(options) { role ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedRole == role) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { viewModel.updateRole(role) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("role_chip_$role")
                    ) {
                        Text(
                            text = role,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedRole == role) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Timeline interactive checkpoints
        item {
            Text(
                text = "Dynamic Timeline Learning roadmaps",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (skillsState.isEmpty()) {
            item {
                Text(
                    text = "Selecting milestones or loading records...",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        } else {
            items(skillsState) { skill ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(20.dp), clip = false),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { viewModel.toggleSkillComplete(skill.id, skill.isCompleted) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Checkbox custom
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (skill.isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (skill.isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                  text = skill.skillName,
                                  fontSize = 14.sp,
                                  fontWeight = FontWeight.Bold,
                                  color = if (skill.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (skill.isCompleted) Color(0xFFD1FAE5) else Color(0xFFF1F5F9))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (skill.isCompleted) "Completed" else "Gap Identified",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (skill.isCompleted) Color(0xFF047857) else Color(0xFF475569)
                                    )
                                }

                                if (!skill.isCompleted) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "+40 XP",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- 8. AI INTERVIEW PREPARATION SCREEN ---

@Composable
fun InterviewPrepScreen(viewModel: CareeronixViewModel) {
    val selectedRole by viewModel.selectedRole.collectAsState()
    val isEvaluating by viewModel.interviewLoading.collectAsState()
    val isCompleted by viewModel.interviewIsCompleted.collectAsState()
    val activeIdx by viewModel.interviewActiveQuestionIndex.collectAsState()
    val rawAnswer by viewModel.currentAnswerText.collectAsState()
    val evaluationFeedback by viewModel.interviewScannedFeedback.collectAsState()

    var activeTextState by remember { mutableStateOf("") }
    val activeQuestion = viewModel.activeQuestion
    val activeList = viewModel.activeQuestionList

    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "AI Mock Interview Prep",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Dynamic technical and behavioral simulator customized for $selectedRole",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        if (!isCompleted) {
            // Screen showing active question card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ROUND: ${activeIdx + 1} OF ${activeList.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.2.sp
                            )

                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { /* Audio simulate voice query */ }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = activeQuestion,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Input answered state
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your response:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )

                        // Voice simulation input button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable {
                                    activeTextState = "I would ensure that layout calculations are offloaded. " +
                                            "React reconciliation triggers updates on corresponding indexes and optimizes component updates."
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Simulate Voice Answer",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = activeTextState,
                        onValueChange = { activeTextState = it },
                        placeholder = { Text("Speak or type comprehensive professional responses here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("interview_answer_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // CTA Submit round trigger
            item {
                Button(
                    onClick = {
                        viewModel.submitAnswer(activeTextState)
                        activeTextState = ""
                    },
                    enabled = !isEvaluating && activeTextState.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_answer_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isEvaluating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("AI Scoring responses...", fontWeight = FontWeight.Bold)
                    } else {
                        Text(
                            text = if (activeIdx == activeList.size - 1) "Finish and AI Analyze" else "Lock Answer & Continue",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // FEEDBACK STAGES SHOWING Circular representation
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "AI EVALUATION FEEDBACK ENGINE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.2.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        evaluationFeedback?.let { report ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                MiniScoreBox("Technical", report.technicalScore, Color(0xFF10B981))
                                MiniScoreBox("Comm.", report.communicationScore, Color(0xFF06B6D4))
                                MiniScoreBox("Confidence", report.confidenceScore, Color(0xFF6366F1))
                            }

                            Divider(modifier = Modifier.padding(vertical = 16.dp))

                            Text(
                                text = "Actionable Improvement Roadmaps:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.align(Alignment.Start)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = report.feedback,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.Start)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { viewModel.restartInterview() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("restart_interview_button")
                        ) {
                            Text("Restart Session", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MiniScoreBox(label: String, score: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${score}%",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}


// --- 9. COLLEGE & INSTITUTION dashboard ---

@Composable
fun CollegeDashboardScreen(viewModel: CareeronixViewModel) {
    val stats = viewModel.initialInstitutionStats

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Campus Employability Dashboard",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            // Premium B2B Subhead
            Text(
                text = stats.collegeName,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold
            )
        }

        // Analytical summary numerical metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCardWidget(
                    metricValue = "${stats.averageEmployabilityIndex}%",
                    metricLabel = "Avg Campus Index",
                    bgColor = Color(0xFFEEF2FF),
                    textColor = Color(0xFF4F46E5),
                    modifier = Modifier.weight(1f)
                )

                MetricCardWidget(
                    metricValue = "${stats.resumeAtsPassRate}%",
                    metricLabel = "ATS Clear Margins",
                    bgColor = Color(0xFFECFDF5),
                    textColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCardWidget(
                    metricValue = "${stats.studentEnrollment}",
                    metricLabel = "Tracked Students",
                    bgColor = Color(0xFFECFEFF),
                    textColor = Color(0xFF06B6D4),
                    modifier = Modifier.weight(1f)
                )

                MetricCardWidget(
                    metricValue = "${stats.activeAtsOptimizerUsers}",
                    metricLabel = "Active Scanning",
                    bgColor = Color(0xFFFFFBEB),
                    textColor = Color(0xFFD97706),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Campus Placements logs table listing package LPA
        item {
            Text(
                text = "Featured Placement Tracks (Tier-2/3 Records)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(stats.recentPlacementsLog) { log ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = log.studentName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${log.targetRole} @ ${log.company}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = log.packageLpa,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF10B981)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = log.badgeUnlocked,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCardWidget(
    metricValue: String,
    metricLabel: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = metricValue,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = textColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = metricLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor.copy(alpha = 0.7f)
            )
        }
    }
}


// --- 10. PROFILE AND EXTRA TRACKER SCREEN ---

@Composable
fun GamifiedGrowthTracker(viewModel: CareeronixViewModel) {
    val profileState by viewModel.profile.collectAsState()
    val rawBadges = profileState?.unlockedBadges ?: "Early Starter"
    val badgeList = rawBadges.split(",")

    val appBadgesList = listOf(
        BadgeItem("Early Starter", "Initiated Career checklist", Icons.Default.RocketLaunch, "Opened Application first time", true),
        BadgeItem("First Mock", "Initiated AI interview Simulator", Icons.Default.Mic, "Submit Mock Interview answers", true),
        BadgeItem("ATS Master", "Scored high compatible ATS ratios", Icons.Default.Description, "Get 85+ Resume matching logs", false),
        BadgeItem("Elite Level 2", "Advanced progression tracker level", Icons.Default.Star, "Unlock 500 XP checkpoints", false),
        BadgeItem("Elite Level 3", "Advanced progression tracker level", Icons.Default.MilitaryTech, "Unlock 1000 XP checkpoints", false)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Gamified Career Growth",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Acquire XP coins, level up, and unlock placements achievements",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Leaderboard visual box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "YOUR PLACEMENT LEADERBOARD STATUS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    profileState?.let { profile ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEEF2FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = Color(0xFF4F46E5),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "Level ${profile.level} Professional",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${profile.xp} Total Career XP",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("logout_button_nav")
                    ) {
                        Text("Log Out Profile Session", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            ExperiencePortfolioSection(viewModel = viewModel)
        }

        item {
            val context = LocalContext.current
            var showPrdDialog by remember { mutableStateOf(false) }
            var downloadMessage by remember { mutableStateOf<String?>(null) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "App Specification & PRD",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Download or view product documentation",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showPrdDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View Specs", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val result = CareeronixPRDExporter.exportPRDToDownloads(context)
                                downloadMessage = result ?: "Failed to save file. Check permissions."
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download PRD", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    downloadMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = msg,
                            fontSize = 11.sp,
                            color = if (msg.contains("Success")) Color(0xFF059669) else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // High Fidelity PRD Modal Dialog
            if (showPrdDialog) {
                AlertDialog(
                    onDismissRequest = { showPrdDialog = false },
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Careeronix Specs & PRD", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { showPrdDialog = false }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close description")
                            }
                        }
                    },
                    text = {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxHeight(0.7f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                PRDSectionCard(
                                    title = "1. Executive Summary & Core Objectives",
                                    icon = Icons.Default.Info,
                                    content = "Careeronix bridges the critical gap between college learning and workspace placements. " +
                                            "It implements live simulation systems across key career categories:\n\n" +
                                            "• Dynamic ATS Resume Scanner & Suggester\n" +
                                            "• Immersive AI Mock Interview Preparation HUD\n" +
                                            "• Personalized Roadmap checklists by Professional roles\n" +
                                            "• High-resolution campus and cohort analytics dashboards for colleges"
                                )
                            }
                            item {
                                PRDSectionCard(
                                    title = "2. Under-the-Hood Database Models",
                                    icon = Icons.Default.Storage,
                                    content = "Our persistent SQLite engine (built via Room DB) utilizes 4 core schema models:\n\n" +
                                            "• UserProfileEntity: Stats tracking email, role, level, and accumulated experience coins (XP).\n" +
                                            "• ResumeRecordEntity: Keeps logs of keyword checks, suggestions, and ATS match ratings.\n" +
                                            "• InterviewRecordEntity: Stores structured interview review checkpoints (comms, raw scoring metrics, evaluations).\n" +
                                            "• SkillRecordEntity: Maps current role checklist elements and completed milestones."
                                )
                            }
                            item {
                                PRDSectionCard(
                                    title = "3. ATS Scanning & Suggestions Engine",
                                    icon = Icons.Default.Description,
                                    content = "Performs dynamic assessments on student cv assets. Calculates real-time ATS index scores by checking:" +
                                            "\n\n- Essential technical skills matching\n- Logical structure validation\n" +
                                            "- High-impact descriptions suggestion\n- Conversion suggestions (e.g., convert paragraphs into quantitative numbers)."
                                )
                            }
                            item {
                                PRDSectionCard(
                                    title = "4. AI Interactive Mock Interview Preparation",
                                    icon = Icons.Default.Mic,
                                    content = "Evaluates candidates across HR, Behavior, or Technical tracks:\n\n" +
                                            "- Focus checks on foundational technologies (e.g., React Concurrent Modes, SQL Indices, STAR structure).\n" +
                                            "- Multi-dimensional scoring on speech confidence, communicability, and deep content quality.\n" +
                                            "- Complete logs matching overall career indexes."
                                )
                            }
                            item {
                                PRDSectionCard(
                                    title = "5. Dynamic Roles Roadmaps",
                                    icon = Icons.Default.Build,
                                    content = "Includes role specializations (Frontend Developer, Data Analyst, UI/UX Designer, AI Engineer). Clicking milestones grants +50 XP, driving continuous professional growth."
                                )
                            }
                            item {
                                PRDSectionCard(
                                    title = "6. Institutional Placement Officers Dashboard",
                                    icon = Icons.Default.School,
                                    content = "Provides a deep administrative portal tracing campus trends:\n\n" +
                                            "- Composite batch job readiness scores\n" +
                                            "- ATS ratings average across the cohort\n" +
                                            "- Interactive charts showing sector and skill distribution across candidates"
                                )
                            }
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Ready to download?",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = "Get the complete formal markdown document (Careeronix_PRD_Documentation.md) and formatted HTML page directly onto your local file-system.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val result = CareeronixPRDExporter.exportPRDToDownloads(context)
                                downloadMessage = result ?: "Failed to save file."
                                showPrdDialog = false
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download PRD", fontSize = 12.sp)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPrdDialog = false }) {
                            Text("Cancel", fontSize = 12.sp)
                        }
                    }
                )
            }
        }

        item {
            Text(
                text = "Placement Badges Collection",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Grids representation using items
        items(appBadgesList) { badge ->
            val unlocked = badgeList.any { it.trim().equals(badge.badgeName, ignoreCase = true) } || badge.defaultUnlock

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (unlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, if (unlocked) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (unlocked) Color(0xFFFFFBEC) else Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = badge.icon,
                            contentDescription = null,
                            tint = if (unlocked) Color(0xFFD97706) else Color(0xFF94A3B8),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = badge.badgeName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Text(
                            text = badge.taskDesc,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (unlocked) 0.6f else 0.3f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (unlocked) Color(0xFFECFDF5) else Color(0xFFF1F5F9))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (unlocked) "Unlocked" else "Locked",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (unlocked) Color(0xFF047857) else Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}

data class BadgeItem(
    val badgeName: String,
    val summary: String,
    val icon: ImageVector,
    val taskDesc: String,
    val defaultUnlock: Boolean
)

@Composable
fun PRDSectionCard(
    title: String,
    icon: ImageVector,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 16.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperiencePortfolioSection(viewModel: CareeronixViewModel) {
    val experiences by viewModel.allExperiences.collectAsState()
    val loading by viewModel.experienceLoading.collectAsState()
    val syncError by viewModel.experienceError.collectAsState()

    var showForm by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<ExperienceRecord?>(null) }

    // Form inputs state
    var title by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var period by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("experience_builder_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PROFESSIONAL PORTFOLIO EXTRAS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Experience Builder & Core Resumé CRUD",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                FilledTonalButton(
                    onClick = {
                        editingEntry = null
                        title = ""
                        company = ""
                        period = ""
                        desc = ""
                        showForm = true
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_experience_trigger")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Experience",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (loading && experiences.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            } else if (experiences.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.WorkOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No recorded experiences yet.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Add portfolio pieces to boost your placement job readiness.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    experiences.forEach { exp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f),
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exp.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${exp.company}  •  ${exp.period}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = exp.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    lineHeight = 16.sp
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        editingEntry = exp
                                        title = exp.title
                                        company = exp.company
                                        period = exp.period
                                        desc = exp.description
                                        showForm = true
                                    },
                                    modifier = Modifier.size(28.dp).testTag("edit_experience_${exp.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit entry",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.deleteExperience(exp.id) },
                                    modifier = Modifier.size(28.dp).testTag("delete_experience_${exp.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete entry",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            syncError?.let { err ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = "Sync Info",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = err,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    if (showForm) {
        AlertDialog(
            onDismissRequest = { showForm = false },
            title = {
                Text(
                    text = if (editingEntry != null) "Edit Portfolio Entry" else "Add Portfolio Experience",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Job Position / Role") },
                        modifier = Modifier.fillMaxWidth().testTag("exp_title_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text("Company / Institution") },
                        modifier = Modifier.fillMaxWidth().testTag("exp_company_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = period,
                        onValueChange = { period = it },
                        label = { Text("Employment Period (e.g., June 2026 - Present)") },
                        modifier = Modifier.fillMaxWidth().testTag("exp_period_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description / Achievements") },
                        modifier = Modifier.fillMaxWidth().testTag("exp_desc_input"),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && company.isNotBlank()) {
                            val entry = editingEntry
                            if (entry != null) {
                                viewModel.updateExperience(entry.id, title, company, period, desc)
                            } else {
                                viewModel.addExperience(title, company, period, desc)
                            }
                            showForm = false
                        }
                    },
                    modifier = Modifier.testTag("exp_submit_btn")
                ) {
                    Text("Save Changes", fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForm = false }) {
                    Text("Cancel", fontSize = 12.sp)
                }
            }
        )
    }
}
