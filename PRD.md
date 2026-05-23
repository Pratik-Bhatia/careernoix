# PRODUCT REQUIREMENTS DOCUMENT (PRD)

## Project Name: Careeronix
**Dynamic AI-Powered Career Readiness, Resume Optimization & Mock Interview Simulator Platform**

---

## 1. Executive Summary & Objectives
Careeronix is a gamified, mobile-first Jetpack Compose application designed to bridge the severe gap between university/college education and industry placement readiness. Moving beyond standard, static checklist tools, Careeronix delivers a highly personalized, interactive preparation experience. 
The application provides real-time simulation analytics for Resume Scanning (ATS scoring), live Technical and HR Mock Interview evaluations, personalized skill gap roadmaps, and an Institutional Dashboard tracking cohort performance. Through an integrated Experience Point (XP) system, dynamic levels, and visual placement badges, the app incentivizes continuous software skill building and interview mock training.

### Core Objectives
1. **Optimize Application Quality**: Build simulated, offline-capable scanner mechanics to test resume relevance against target industry titles.
2. **Immersive Interview Training**: Similate interview situations under stressful timelines to output clear scores on comms, technical capability, and confidence levels.
3. **Institutional Diagnostics**: Empower colleges/universities to trace batch metrics, cohort average progress, and readiness index ratios for proactive placement actions.
4. **Interactive Engagement**: Gamify learning via levels, dynamic badges, leaderboards, and immediate career progress feedback loops.

---

## 2. User Personas
*   **Persona A (Pratik Bhatia - The Aspiring Developer)**: A third-year frontend engineering student with high confidence but lack of understanding regarding modern ATS keyword filtering and technical React hooks. Needs a clear role roadmap and actionable resume diagnostic suggestions.
*   **Persona B (College Placement Officer / Admin)**: A college career officer monitoring a cohort of 500+ candidates, needing clear visibility into the batch readiness index, average composite scores, and top skills deficiencies.

---

## 3. Comprehensive Feature Architecture

### A. Auth Screen & JWT Simulation
Provides secure registration and login routines, establishing local state verification.
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
*   **Dynamic Roadmaps**: Instantaneous load of role-specific modules.
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

---

## 6. How to Download / Export this Document inside the App
Careeronix packages a dynamic **PRD & Documentation Viewer** directly in the profile settings tab. Clicking the **"Download PRD Document"** button triggers an immediate write of this full markdown guide and its interactive HTML companion directly to your phone's storage at:
`/storage/emulated/0/Download/Careeronix_PRD_Documentation.md`
`/storage/emulated/0/Download/Careeronix_PRD_Documentation.html`

The local file is saved using custom scoped content providers and can be instantly pulled via Android File Transfer or viewed natively on your system!
