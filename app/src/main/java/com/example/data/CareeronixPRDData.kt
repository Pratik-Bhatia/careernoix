package com.example.data

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object CareeronixPRDExporter {

    const val PRD_MARKDOWN_CONTENT = """# PRODUCT REQUIREMENTS DOCUMENT (PRD)

## Project Name: Careeronix
**Dynamic AI-Powered Career Readiness, Resume Optimization & Mock Interview Simulator Platform**

---

## 1. Executive Summary & Objectives
Careeronix is a gamified, mobile-first Jetpack Compose application designed to bridge the severe gap between university/college education and industry placement readiness. Moving beyond standard, static checklist tools, Careeronix delivers a highly personalized, interactive preparation experience. 
The application provides real-time simulation analytics for Resume Scanning (ATS scoring), live Technical and HR Mock Interview evaluations, personalized skill gap roadmaps, and an Institutional Dashboard tracking cohort performance. Through an integrated Experience Point (XP) system, dynamic levels, and visual placement badges, the app incentivizes continuous software skill building and interview mock training.

### Core Objectives
1. **Optimize Application Quality**: Build simulated, offline-capable scanner mechanics to test resume relevance against target industry titles.
2. **Immersive Interview Training**: Immersive real-time interview scenarios that grade technical knowledge, speech confidence, and overall communicative impact.
3. **Institutional Diagnostics**: Empower colleges/universities to trace batch metrics, cohort average progress, and readiness index ratios for proactive placement actions.
4. **Interactive Engagement**: Gamify learning via levels, dynamic badges, leaderboards, and immediate career progress feedback loops.

---

## 2. User Personas
*   **Persona A (Pratik Bhatia - The Aspiring Developer)**: A third-year frontend engineering student with high confidence but lack of understanding regarding modern ATS keyword filtering and technical React hooks. Needs a clear role roadmap and actionable resume diagnostic suggestions.
*   **Persona B (College Placement Officer / Admin)**: A college career officer monitoring a cohort of 500+ candidates, needing clear visibility into the batch readiness index, average composite scores, and top skills deficiencies.

---

## 3. Comprehensive Feature Architecture

### A. Auth Screen & JWT Simulation
Provides registration and login routines, establishing local state verification.
*   **Input Controls**: Dedicated text fields for Email and Name verification with filled M3 styling.
*   **Onboarding Flow**: Integrated 4-step wizard card depicting Careeronix's value propositions.
*   **JWT Sim**: Simulates backend authentication handshakes, producing standard local profile caching upon success.

### B. Resume Scan Simulator (ATS Engine)
Performs extensive diagnostic checks on college resumes against role keywords.
*   **Document Selection**: Pre-seeded documents or custom pdf structures with distinct matching scopes.
*   **Interactive Diagnostic Loop**: Step-by-step progress logging (formatting scan, parser ingestion, keyword matching).
*   **Score Outputs**: Provides composite ATS score (0-100%), detailed Critical Weaknesses list, and customized actionable Suggestions.

### C. Live AI Interview Preparation Simulator
Immersive interactive mock environment simulating Technical, HR, and Behavioral hiring tracks.
*   **Track Selection**: High-contrast controls to select between HR, Technical, and Behavioral categories.
*   **Dynamic Q&A HUD**: Standard progression index showing questions tracking actual interview banks (React Hooks, SQL indices, STAR techniques).
*   **Performance Analytics**: Detailed communication level meters, technical competency breakdowns, and overall confidence metrics.

### D. Skill-Gap Roadmap & Checklist
Tailored professional skill milestones grouped by chosen job sectors (Frontend Developer, Data Analyst, UI/UX Designer, AI Engineer).
*   **Checklist Interaction**: Tap to complete items. Completion rewards users with +50 XP, advancing learner levels.

### E. Institutional Analytics Dashboard (College Portal)
Exposes complete statistical projections tracking cohort ready records.
*   **Core Metrics Row**: Instant statistics outlining average ATS rating, total mock completions, and global campus job readiness.
*   **Ready Percent Progress**: Radial tracking gauge showing placement target percent goals.
*   **Sector Representation**: Elegant breakdown chart representing cohort performance across different role specializations.

### F. Gamified Profile & Leaderboard HUD
Centralized hub tracking gamified status.
*   **Placement Badges**: Tracks unlocked rewards (e.g. "Early Starter", "First Mock", "ATS Master", "Elite Professional").
*   **Experience Indicator**: Displays total career XP, learner tier progress, and profile sessions.

---

## 4. Under-the-Hood Technical Workflows

### Database Architecture
Powered by standard Room Database persistence utilizing SQLite. 
*   **UserProfile Entity**: Contains core stats (xp, level, targetRole, onboardingCompleted, unlockedBadges, status scores).
*   **ResumeRecord Entity**: Tracks historical scans (filename, atsScore, weaknesses, suggestions, layout ratings).
*   **InterviewRecord Entity**: Keeps log of candidate assessments (overallScore, communicationScore, technicalScore, confidenceScore, feedback).
*   **SkillRecord Entity**: Stores the user's customized skills roadmap progression and completion statuses.

### State Flow Management
Utilizes Jetpack Compose's state-sharing paradigm with a central `CareeronixViewModel` communicating with a local `CareeronixRepository`. Under-the-hood events utilize `MutableStateFlow` converted to clean UI States (`StateFlow`) using `SharingStarted.WhileSubscribed(5000)` to preserve memory and handle lifecycle events.

---

## 5. Security and Execution Policies
1. **Isolated Storage**: Local SQLite DB maintains data structures offline beneath scoped system partitions.
2. **Safety Handling**: Built-in main lifecycle crash-interceptors intercept state blockages or DB schema updates, offering a seamless "Reset & Recover App Database" action widget.
3. **Verification**: Robust test verification via local unit tests, Robolectric framework execution, and compiler lint checks.
"""

    const val PRD_HTML_CONTENT = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Careeronix - Product Requirements Document (PRD)</title>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            color: #1e293b;
            background-color: #f8fafc;
            line-height: 1.6;
            margin: 0;
            padding: 40px 20px;
        }
        .container {
            max-width: 800px;
            background: #ffffff;
            margin: 0 auto;
            padding: 40px;
            border-radius: 12px;
            box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
        }
        h1 {
            color: #3b82f6;
            font-size: 2.25rem;
            margin-bottom: 0.5rem;
            border-bottom: 2px solid #e2e8f0;
            padding-bottom: 12px;
        }
        h2 {
            color: #1e3a8a;
            font-size: 1.5rem;
            margin-top: 2rem;
            border-bottom: 1px solid #f1f5f9;
            padding-bottom: 6px;
        }
        h3 {
            color: #2563eb;
            font-size: 1.2rem;
            margin-top: 1.5rem;
        }
        p, ul {
            font-size: 1rem;
            color: #475569;
        }
        li {
            margin-bottom: 8px;
        }
        .badge {
            background-color: #dbeafe;
            color: #1e40af;
            font-size: 0.75rem;
            font-weight: bold;
            padding: 4px 8px;
            border-radius: 9999px;
            display: inline-block;
            margin-bottom: 20px;
        }
        hr {
            border: 0;
            border-top: 1px dashed #e2e8f0;
            margin: 2rem 0;
        }
    </style>
</head>
<body>
    <div class="container">
        <span class="badge">Careeronix app specs</span>
        <h1>Careeronix: Product Requirements Document</h1>
        <p><strong>Dynamic AI-Powered Career Readiness, Resume Optimization & Mock Interview Simulator Platform</strong></p>
        
        <h2>1. Executive Summary & Objectives</h2>
        <p>Careeronix is a gamified, mobile-first Jetpack Compose application designed to bridge the severe gap between university/college education and industry placement readiness. Moving beyond standard, static checklist tools, Careeronix delivers a highly personalized, interactive preparation experience.</p>
        <p>The application provides real-time simulation analytics for Resume Scanning (ATS scoring), live Technical and HR Mock Interview evaluations, personalized skill gap roadmaps, and an Institutional Dashboard tracking cohort performance. Through an integrated Experience Point (XP) system, dynamic levels, and visual placement badges, the app incentivizes continuous software skill building and interview mock training.</p>
        
        <h3>Core Objectives</h3>
        <ul>
            <li><strong>Optimize Application Quality:</strong> Build simulated, offline-capable scanner mechanics to test resume relevance against target industry titles.</li>
            <li><strong>Immersive Interview Training:</strong> Immersive real-time interview scenarios that grade technical knowledge, speech confidence, and overall communicative impact.</li>
            <li><strong>Institutional Diagnostics:</strong> Empower colleges/universities to trace batch metrics, cohort average progress, and readiness index ratios for proactive placement actions.</li>
            <li><strong>Interactive Engagement:</strong> Gamify learning via levels, dynamic badges, leaderboards, and immediate career progress feedback loops.</li>
        </ul>

        <hr>

        <h2>2. User Personas</h2>
        <ul>
            <li><strong>Persona A (Pratik Bhatia - The Aspiring Developer):</strong> A third-year frontend engineering student with high confidence but lack of understanding regarding modern ATS keyword filtering and technical React hooks. Needs a clear role roadmap and actionable resume diagnostic suggestions.</li>
            <li><strong>Persona B (College Placement Officer / Admin):</strong> A college career officer monitoring a cohort of 500+ candidates, needing clear visibility into the batch readiness index, average composite scores, and top skills deficiencies.</li>
        </ul>

        <hr>

        <h2>3. Comprehensive Feature Architecture</h2>
        <h3>A. Auth Screen & JWT Simulation</h3>
        <p>Provides registration and login routines, establishing local state verification.</p>
        <ul>
            <li><strong>Input Controls:</strong> Dedicated text fields for Email and Name verification with filled M3 styling.</li>
            <li><strong>Onboarding Flow:</strong> Integrated 4-step wizard card depicting Careeronix's value propositions.</li>
            <li><strong>JWT Sim:</strong> Simulates backend authentication handshakes, producing standard local profile caching upon success.</li>
        </ul>

        <h3>B. Resume Scan Simulator (ATS Engine)</h3>
        <p>Performs extensive diagnostic checks on college resumes against role keywords.</p>
        <ul>
            <li><strong>Document Selection:</strong> Pre-seeded documents or custom pdf structures with distinct matching scopes.</li>
            <li><strong>Interactive Diagnostic Loop:</strong> Step-by-step progress logging (formatting scan, parser ingestion, keyword matching).</li>
            <li><strong>Score Outputs:</strong> Provides composite ATS score (0-100%), detailed Critical Weaknesses list, and customized actionable Suggestions.</li>
        </ul>

        <h3>C. Live AI Interview Preparation Simulator</h3>
        <p>Immersive interactive mock environment simulating Technical, HR, and Behavioral hiring tracks.</p>
        <ul>
            <li><strong>Track Selection:</strong> High-contrast controls to select between HR, Technical, and Behavioral categories.</li>
            <li><strong>Dynamic Q&A HUD:</strong> Standard progression index showing questions tracking actual interview banks (React Hooks, SQL indices, STAR techniques).</li>
            <li><strong>Performance Analytics:</strong> Detailed communication level meters, technical competency breakdowns, and overall confidence metrics.</li>
        </ul>

        <h3>D. Skill-Gap Roadmap & Checklist</h3>
        <p>Tailored professional skill milestones grouped by chosen job sectors (Frontend Developer, Data Analyst, UI/UX Designer, AI Engineer).</p>
        <ul>
            <li><strong>Checklist Interaction:</strong> Tap to complete items. Completion rewards users with +50 XP, advancing learner levels.</li>
        </ul>

        <h3>E. Institutional Analytics Dashboard (College Portal)</h3>
        <p>Exposes complete statistical projections tracking cohort ready records.</p>
        <ul>
            <li><strong>Core Metrics Row:</strong> Instant statistics outlining average ATS rating, total mock completions, and global campus job readiness.</li>
            <li><strong>Ready Percent Progress:</strong> Radial tracking gauge showing placement target percent goals.</li>
            <li><strong>Sector Representation:</strong> Elegant breakdown chart representing cohort performance across different role specializations.</li>
        </ul>

        <h3>F. Gamified Profile & Leaderboard HUD</h3>
        <p>Centralized hub tracking gamified status.</p>
        <ul>
            <li><strong>Placement Badges:</strong> Tracks unlocked rewards (e.g. "Early Starter", "First Mock", "ATS Master", "Elite Professional").</li>
            <li><strong>Experience Indicator:</strong> Displays total career XP, learner tier progress, and profile sessions.</li>
        </ul>

        <hr>

        <h2>4. Under-the-Hood Technical Workflows</h2>
        <h3>Database Architecture</h3>
        <p>Powered by Room Database persistence utilising sqlite platform underneath.</p>
        <ul>
            <li><strong>UserProfile Entity:</strong> Contains core stats (xp, level, targetRole, onboardingCompleted, unlockedBadges, status scores).</li>
            <li><strong>ResumeRecord Entity:</strong> Tracks historical scans (filename, atsScore, weaknesses, suggestions, layout ratings).</li>
            <li><strong>InterviewRecord Entity:</strong> Keeps log of candidate assessments (overallScore, communicationScore, technicalScore, confidenceScore, feedback).</li>
            <li><strong>SkillRecord Entity:</strong> Stores the user's customized skills roadmap progression and completion statuses.</li>
        </ul>

        <hr>

        <h2>5. How to Retrieve Downloaded Copy</h2>
        <p>The documents have been written directly to your local emulator storage under <code>DIRECTORY_DOWNLOADS</code>.</p>
        <p>Files created:</p>
        <ol>
            <li><code>Careeronix_PRD_Documentation.md</code></li>
            <li><code>Careeronix_PRD_Documentation.html</code></li>
        </ol>
    </div>
</body>
</html>"""

    fun exportPRDToDownloads(context: Context): String? {
        return try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }

            val mdFile = File(downloadDir, "Careeronix_PRD_Documentation.md")
            FileOutputStream(mdFile).use { fos ->
                fos.write(PRD_MARKDOWN_CONTENT.toByteArray())
            }

            val htmlFile = File(downloadDir, "Careeronix_PRD_Documentation.html")
            FileOutputStream(htmlFile).use { fos ->
                fos.write(PRD_HTML_CONTENT.toByteArray())
            }

            Log.d("PRDExporter", "Files successfully created under public Downloads folder")
            "Success! Exported to your public Downloads directory: ${mdFile.absolutePath}"
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to internal storage downloads
            try {
                val fallbackDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                if (fallbackDir != null) {
                    if (!fallbackDir.exists()) {
                        fallbackDir.mkdirs()
                    }
                    val fallbackMdFile = File(fallbackDir, "Careeronix_PRD_Documentation.md")
                    FileOutputStream(fallbackMdFile).use { fos ->
                        fos.write(PRD_MARKDOWN_CONTENT.toByteArray())
                    }
                    val fallbackHtmlFile = File(fallbackDir, "Careeronix_PRD_Documentation.html")
                    FileOutputStream(fallbackHtmlFile).use { fos ->
                        fos.write(PRD_HTML_CONTENT.toByteArray())
                    }
                    "Success! Exported to fallback app downloads directory: ${fallbackMdFile.absolutePath}"
                } else {
                    null
                }
            } catch (fallbackEx: Exception) {
                fallbackEx.printStackTrace()
                null
            }
        }
    }
}
