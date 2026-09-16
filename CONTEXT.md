# MentorAI — Project Context

## 1. Project Overview

**MentorAI** is a personal AI-powered career navigation and mentorship platform designed primarily for college students, especially BCA/CS students who struggle to understand what they should learn, which career path fits them, and what the current job market actually demands.

The core problem comes from a real student experience:

> A student can spend 1–2 years learning technologies or following career paths without understanding whether those paths fit their interests, goals, current abilities, and the actual job market.

MentorAI should reduce this uncertainty by connecting four things:

**Student Profile + Goals + Real Market Intelligence + Adaptive Mentorship**

MentorAI is NOT intended to be a generic chatbot that says "learn Python, SQL and DSA."

It should act as a **career navigation system** that continuously evaluates a student's situation against real-world market signals and produces evidence-backed recommendations and an adaptive roadmap.

---

# 2. Core Vision

### Vision

Build an AI career mentor that helps students answer:

- What career paths fit me?
- What does the market currently look like?
- Is a career I am interested in actually realistic for me?
- What skills do employers currently expect?
- What should I learn first?
- What should I NOT learn yet?
- Which projects should I build?
- How far am I from being job-ready?
- Which jobs match my current skills?
- What should I change if the market or my goals change?

### Product Positioning

Do NOT position MentorAI as:

> "An AI that tells students which career to choose."

Position it as:

> **"An AI career navigation system that continuously connects a student's interests, skills and goals with real-world job-market signals and builds an adaptive roadmap toward their target career."**

---

# 3. Target Users

Primary users:

- BCA students
- BSc CS/IT students
- Computer science students
- Early-career developers
- College students who are unsure about specialization

Initial focus should be BCA/CS students because the project originates from a real BCA student problem.

Do not over-expand the product initially.

---

# 4. Core User Flow

The primary flow should be:

```text
Student Profile
      ↓
Interests + Goals + Current Skills
      ↓
Market Intelligence
      ↓
Career Analysis
      ↓
Career Options
      ↓
Career Reality Check
      ↓
Recommended Career Path
      ↓
Skill Gap Analysis
      ↓
Personalized Roadmap
      ↓
Projects + Learning Tasks
      ↓
Progress Tracking
      ↓
Job Readiness
      ↓
Job Matching
      ↓
Continuous Roadmap Adaptation
```

---

# 5. Core Functionalities

## 5.1 Student Onboarding / Profile

MentorAI should collect enough information to personalize recommendations.

Profile fields should include:

- Name / display name
- Degree
- Current year/semester
- Current technical skills
- Skill proficiency
- Programming languages
- Interests
- Preferred domains
- Career goals
- Salary expectations (optional)
- Preferred location
- Remote/on-site preference
- Time available per week
- Current projects
- Internship/work experience
- Certifications
- Academic strengths
- Areas the student dislikes
- Short-term goals
- Long-term goals

The system should allow the user to update their profile at any time.

---

# 6. Career Discovery

MentorAI should analyze the user's profile and identify several plausible career paths.

Examples:

- Full-stack developer
- Backend developer
- Frontend developer
- Mobile developer
- Data analyst
- Data engineer
- AI/ML engineer
- Cloud/DevOps engineer
- Cybersecurity
- QA/Automation
- Product/technical roles
- Other relevant paths

Do not hardcode the final recommendation.

The system should use career data + market data + user profile to calculate/reason about fit.

Each career option should include:

- Career name
- Description
- Why it may fit
- Required skills
- Current market demand
- Entry-level difficulty
- Typical job titles
- Typical responsibilities
- Approximate learning effort
- Important technologies
- Risks/trade-offs
- Alternative paths

---

# 7. Career Reality Check

This should be one of MentorAI's signature features.

Before recommending a career, MentorAI should explain the reality of that path.

For each career:

- Market demand
- Entry-level accessibility
- Competition
- Typical skill requirements
- Degree relevance
- Typical experience expectations
- Certifications that matter
- Portfolio/project expectations
- Common misconceptions
- Current trends
- Potential risks
- What beginners often underestimate
- What is hype vs what is actually useful

Example:

> AI/ML is growing rapidly, but entry-level AI/ML roles may require stronger mathematics, programming, projects, internships, or higher qualifications than many beginners expect.

The system should clearly distinguish:

**Market trend ≠ easy entry-level opportunity.**

---

# 8. Reality vs Hype

MentorAI should actively challenge popular assumptions.

Examples:

- "AI is the future, so everyone should become an ML engineer."
- "Cybersecurity is always the best field."
- "Learn 15 technologies to get a job."
- "A certification guarantees a job."

MentorAI should evaluate claims using available market evidence and explain:

- What is genuinely supported by data
- What is exaggerated
- What applies to the particular student
- What does not apply

This feature is important because the target user is often inexperienced and vulnerable to technology hype.

---

# 9. Market Intelligence

This is one of the most important components of the system.

MentorAI must not rely solely on the LLM's internal knowledge when making current-market claims.

The system should collect and process legitimate/public market data.

Potential data sources can include:

- Public job listings
- Public company career pages
- Public technology reports
- Public salary reports
- Public industry reports
- Other legally accessible sources

Do not bypass authentication, paywalls, robots restrictions, CAPTCHAs, or website protections.

The system should respect terms of service and applicable laws.

---

# 10. Market Data Pipeline

The intended pipeline is:

```text
Public Data Sources
        ↓
Data Collection
        ↓
Cleaning / Normalization
        ↓
Job / Skill Extraction
        ↓
Structured Database
        ↓
Trend Analysis
        ↓
Market Snapshot
        ↓
AI Reasoning
        ↓
Student Recommendation
```

Important principle:

> **The LLM should interpret market evidence, not invent market evidence.**

For example, if the database finds that a particular skill appears in 47% of collected relevant job listings, the AI can explain what that means.

The AI should not simply assert that the skill is popular without evidence.

---

# 11. Market Snapshot

For each career, MentorAI should be able to produce a market snapshot.

Example structure:

```text
Career: Backend Developer

Market Demand:
High

Entry Difficulty:
Medium

Common Skills:
Java
Spring Boot
SQL
REST APIs
Git
Docker
Cloud

Emerging Skills:
...

Common Job Titles:
...

Skill Frequency:
Java       61%
SQL        74%
REST APIs  68%
Docker     42%

Trend:
...

Last Updated:
...
```

Percentages should only be shown when calculated from actual collected data.

The system should show the source/data collection date where practical.

---

# 12. Career Matching

MentorAI should compare:

```text
Student
   ↕
Career
```

using:

- Interests
- Goals
- Current skills
- Skill gaps
- Preferences
- Market demand
- Entry difficulty
- Learning effort
- User constraints

Each recommendation should explain **why** it was made.

Example:

```text
Recommended Path: Backend Development

Fit: 86%

Why:
- Strong interest in programming
- Existing Java knowledge
- Goal is software employment
- Current market contains substantial backend demand
- Current skills transfer naturally into Spring Boot development

Main gaps:
- Spring Boot
- REST APIs
- SQL depth
- Docker
- Testing
```

Avoid pretending that the fit percentage is mathematically perfect. It should be treated as a decision-support indicator.

---

# 13. "Why This Path?"

Every recommendation should be explainable.

MentorAI should provide:

- Reasons based on the student's profile
- Relevant market evidence
- Required skills
- Risks
- Alternatives
- Confidence/strength of recommendation
- Important assumptions

Never return only:

> "You should become a backend developer."

Instead explain the reasoning.

---

# 14. "What Should I NOT Learn Yet?"

This is a key feature.

MentorAI should actively prioritize learning instead of endlessly adding technologies.

Example:

```text
You want to become an AI engineer.

Do NOT prioritize:
- Advanced PyTorch
- Kubernetes
- Complex MLOps

Yet.

First focus on:
1. Python
2. Programming fundamentals
3. Data structures
4. SQL
5. Statistics
6. ML fundamentals
```

The system should be able to explain why certain technologies are being postponed.

---

# 15. Personalized Roadmap

Once a career is selected, generate a roadmap from:

```text
Current Student State
        ↓
Target Career
        ↓
Required Skills
        ↓
Skill Gaps
        ↓
Learning Sequence
        ↓
Projects
        ↓
Milestones
        ↓
Job Preparation
```

Roadmap should include:

- Phase
- Skill
- Learning objective
- Recommended resources
- Practice tasks
- Project
- Expected outcome
- Estimated effort
- Dependencies
- Completion criteria

The roadmap should prioritize rather than overwhelm.

---

# 16. Project Recommendations

Projects should be selected based on skill gaps and target roles.

Do NOT generate random projects.

Example:

Target: Backend Developer

```text
Project 1
REST API
→ Learn HTTP, REST, controllers, services

Project 2
Authentication System
→ Learn Spring Security, JWT, authorization

Project 3
Production-style Backend
→ Learn Docker, testing, caching, database design

Project 4
Capstone
→ Combine major skills
```

Each project should explain:

- Skills developed
- Why the project matters
- What employers can infer from it
- Difficulty
- Expected deliverables
- Optional extensions

---

# 17. Progress Tracking

Students should be able to mark roadmap items as:

- Not started
- In progress
- Completed
- Skipped
- Needs review

Track:

- Skills
- Projects
- Learning tasks
- Milestones
- Time spent
- Self-assessed confidence

MentorAI should use this data to update recommendations.

---

# 18. Adaptive Mentorship

MentorAI should NOT be a one-time roadmap generator.

It should continuously adapt.

Example:

```text
Original Roadmap
      ↓
Student Progress
      ↓
New Skills
      ↓
Market Changes
      ↓
New Goals
      ↓
Gap Recalculation
      ↓
Updated Roadmap
```

Example:

> You completed the backend fundamentals earlier than expected.

> Your current market target also increasingly values Docker and cloud deployment.

> Your next phase has therefore been adjusted.

---

# 19. Weekly Mentor Mode

MentorAI should eventually provide a weekly check-in.

Example:

```text
What did you complete this week?

What did you struggle with?

How many hours did you spend?

What do you want to focus on next week?
```

Then MentorAI can:

- Review progress
- Identify blockers
- Adjust the schedule
- Suggest the next tasks
- Recommend revision
- Prevent unrealistic workloads

---

# 20. Skill Gap Analysis

Compare:

```text
Student Skills
       ↓
Target Career Requirements
       ↓
Missing Skills
```

Example:

```text
Backend Developer Readiness

Java          █████████░  90%
SQL           ███████░░░  70%
REST APIs     ██████░░░░  60%
Spring Boot   ████░░░░░░  40%
Docker        ██░░░░░░░░  20%
Testing       █████░░░░░  50%
```

The system should explain the most important gaps rather than simply showing a score.

---

# 21. Job Readiness Score

MentorAI should eventually estimate how prepared the student is for their target roles.

Readiness should consider:

- Required technical skills
- Project experience
- Relevant tools
- Interview preparation
- Resume alignment
- Job description requirements

Do not create an arbitrary score.

The score should be explainable and based on defined criteria.

Example:

```text
Overall readiness: 64%

Strong:
- Java
- Git
- SQL

Needs improvement:
- Spring Boot
- Docker
- Testing
- Production deployment

Biggest immediate gap:
Spring Boot + REST API development
```

---

# 22. Job Description Decoder

Allow a student to paste or provide a job description.

MentorAI should analyze:

- Required skills
- Preferred skills
- Experience requirements
- Responsibilities
- Technologies
- Missing skills
- Match percentage
- Priority gaps

Example:

```text
Job Match: 71%

You have:
✓ Java
✓ SQL
✓ Git

You are missing:
✗ Docker
✗ AWS

Partial:
~ Spring Boot

Recommendation:
You can apply, but prioritize Spring Boot and Docker.
```

The system should distinguish between:

**Required**
and
**Nice to have**

---

# 23. Job Matching

Eventually MentorAI should allow students to discover relevant jobs based on their profile.

Flow:

```text
Student Profile
      ↓
Skill Representation
      ↓
Job Embeddings / Skill Matching
      ↓
Relevant Jobs
      ↓
Match Analysis
```

Each job should show:

- Match level
- Skills matched
- Skills missing
- Why it is relevant
- Target career
- Location
- Source
- Collection date

---

# 24. Career Path Simulator

Students should eventually be able to compare paths.

Example:

```text
Path A: Backend Development
Path B: Data Analytics
Path C: Cybersecurity
```

Compare:

- Skill requirements
- Entry difficulty
- Learning effort
- Market demand
- Common job titles
- Typical projects
- Student fit
- Transferable skills
- Risks

The user should be able to ask:

> "What happens if I choose Path B instead?"

---

# 25. Path Change / Pivot Detection

Students may change interests.

MentorAI should support career pivots.

Example:

```text
Current:
Backend Developer

New Interest:
Data Engineering
```

MentorAI should calculate:

- Transferable skills
- Existing advantages
- New skills required
- What can be skipped
- Estimated transition effort
- New roadmap

The student should not be forced to start from zero.

---

# 26. AI Mentor Chat

A conversational interface should eventually exist.

The mentor should be able to answer questions using:

- Student profile
- Current roadmap
- Progress
- Market data
- Career requirements
- Job data

Example:

> "Should I learn Kubernetes now?"

MentorAI should answer based on the student's current stage and target path, not give generic advice.

---

# 27. AI Architecture

Use Java/Spring Boot as the primary backend.

Preferred architecture:

```text
                         Next.js
                         Frontend
                            │
                            ↓
                    Spring Boot API
                            │
        ┌───────────────────┼────────────────────┐
        │                   │                    │
        ↓                   ↓                    ↓
  Profile Service      Mentor/AI Service   Market Service
        │                   │                    │
        │              Spring AI                │
        │                   │                    │
        │                Ollama                  │
        │                   │                    │
        └─────────────── PostgreSQL ─────────────┘
                              │
                           pgvector
```

---

# 28. Technology Stack

## Backend

- Java 25+
- Spring Boot
- Spring AI
- Spring Web
- Spring Data JPA
- Spring Security
- Bean Validation
- Maven

## AI

- Ollama
- Open-source local models such as Qwen/Gemma or another suitable model
- Spring AI
- Local embeddings where practical

## Database

- PostgreSQL
- pgvector

## Market Data

- Spring WebClient
- Jsoup
- Scheduled jobs using Spring Scheduler
- Playwright only when necessary for legitimate browser automation

## Frontend

- Next.js
- React
- TypeScript
- Tailwind CSS

## Infrastructure

- Docker
- Git
- GitHub

Everything should be open-source/free for local development wherever practical.

---

# 29. Local-First Principle

MentorAI should follow:

> **Local-first, open-source-first, free-first.**

Preferred order:

1. Local/open-source tool
2. Free open-source library
3. Free hosted service
4. Paid service only when genuinely necessary

Avoid unnecessary paid APIs.

The project should be capable of running locally with:

```text
Next.js
+
Spring Boot
+
PostgreSQL
+
Ollama
```

---

# 30. AI Model Principle

The LLM should NOT be treated as the database or source of truth.

Use this pattern:

```text
Data
 ↓
Structured Analysis
 ↓
Evidence
 ↓
LLM
 ↓
Explanation / Recommendation
```

Avoid:

```text
User
 ↓
LLM
 ↓
Random market claim
```

The system should clearly distinguish:

- Observed data
- Calculated metrics
- AI interpretation
- AI recommendation
- Uncertainty

---

# 31. RAG / Vector Search

Use pgvector for semantic retrieval.

Potential vectorized objects:

- Job descriptions
- Career descriptions
- Skill descriptions
- Market reports
- Learning resources
- Project descriptions

Potential flow:

```text
Document
 ↓
Chunk
 ↓
Embedding Model
 ↓
pgvector
 ↓
Semantic Search
 ↓
Relevant Context
 ↓
LLM
```

Do not introduce a separate vector database unless PostgreSQL/pgvector becomes insufficient.

---

# 32. Agent Architecture

Do not over-engineer the agent system initially.

Use Spring services with clear responsibilities.

Potential agents/services:

```text
MarketResearchAgent
CareerAnalysisAgent
SkillGapAgent
RoadmapAgent
JobMatchAgent
ProgressAgent
MentorAgent
```

Possible orchestration:

```text
Student Profile
      ↓
Market Research
      ↓
Career Analysis
      ↓
Skill Gap
      ↓
Roadmap
      ↓
Project Recommendations
```

Later, build a dedicated orchestration layer if needed.

Do not introduce Python/LangChain/LangGraph merely because they are popular.

The primary implementation should remain Java/Spring Boot/Spring AI.

---

# 33. Database Concept

Initial entities:

```text
User
StudentProfile
Skill
StudentSkill
Career
CareerSkill
Job
JobSkill
MarketSnapshot
Roadmap
RoadmapPhase
RoadmapTask
Project
UserProject
ProgressRecord
MentorConversation
MentorMessage
LearningResource
```

Possible relationships:

```text
StudentProfile
    ↓
StudentSkill
    ↓
Skill
    ↑
CareerSkill
    ↑
Career
```

And:

```text
Job
 ↓
JobSkill
 ↓
Skill
```

This creates a shared skill graph between students, careers and jobs.

---

# 34. Important Data Model Concept

Skills should be normalized.

Do not store:

```text
"Java, Spring Boot, SQL"
```

as one string everywhere.

Use:

```text
Skill
-----
id
name
category
description
```

Then relations:

```text
StudentSkill
CareerSkill
JobSkill
```

This makes skill-gap analysis and market analysis much easier.

---

# 35. Security

Use Spring Security.

Eventually support:

- Authentication
- Authorization
- Password hashing
- Session/JWT strategy
- User-specific data access

Never expose another user's profile, roadmap, conversations, or private data.

Keep secrets/configuration in environment variables.

Never commit:

- API keys
- passwords
- database credentials
- private tokens

---

# 36. API Design

Use REST APIs.

Example:

```text
POST   /api/auth/register
POST   /api/auth/login

GET    /api/profile
PUT    /api/profile

GET    /api/careers
POST   /api/careers/analyze

GET    /api/market
GET    /api/market/careers/{careerId}

POST   /api/roadmaps
GET    /api/roadmaps/current
PUT    /api/roadmaps/{id}

POST   /api/mentor/chat

POST   /api/jobs/analyze
POST   /api/jobs/match

GET    /api/progress
PUT    /api/progress/{id}
```

Keep API design clean and versionable.

---

# 37. UI / UX Principles

The UI should feel like a career dashboard rather than a chatbot.

Main dashboard:

```text
┌───────────────────────────────────────────┐
│              MentorAI                     │
├───────────────────────────────────────────┤
│ Career Direction                          │
│ Backend Development                       │
│                                           │
│ Market: High Demand                       │
│ Readiness: 64%                            │
│                                           │
│ Current Focus                             │
│ Spring Boot + REST APIs                   │
│                                           │
│ This Week                                 │
│ □ Build REST API                           │
│ □ Practice SQL joins                       │
│ □ Complete authentication module           │
│                                           │
│ Market Update                             │
│ Docker demand increased in target roles    │
│                                           │
│ Ask MentorAI                              │
└───────────────────────────────────────────┘
```

Avoid making the home screen just a chat box.

---

# 38. MVP

The first version MUST remain small.

### MVP Version 1

Implement only:

1. User profile
2. Interests/goals collection
3. Career discovery
4. Career Reality Check
5. Market snapshot using a small controlled dataset
6. Career recommendation
7. Skill-gap analysis
8. Personalized roadmap
9. Basic mentor chat
10. Progress tracking

Do NOT build everything at once.

---

# 39. Development Phases

## Phase 1 — Foundation

Build:

- Spring Boot project
- PostgreSQL
- Basic entities
- REST APIs
- Next.js frontend
- Basic authentication
- Profile management

Goal:

> User can create and manage their student profile.

---

## Phase 2 — AI Mentor

Add:

- Ollama
- Spring AI
- Prompt templates
- Structured AI outputs
- Career analysis
- Roadmap generation

Goal:

> User can receive a personalized career recommendation and roadmap.

---

## Phase 3 — Market Intelligence

Add:

- Data collection
- Jsoup
- Public data sources
- Database storage
- Skill extraction
- Market metrics
- Market snapshots

Goal:

> Recommendations are based on actual collected market evidence.

---

## Phase 4 — RAG

Add:

- Embeddings
- pgvector
- Semantic search
- Retrieval
- Evidence-aware responses

Goal:

> MentorAI can retrieve relevant market/job information before answering.

---

## Phase 5 — Adaptive Mentor

Add:

- Progress tracking
- Weekly check-ins
- Skill-gap recalculation
- Roadmap adjustment
- Career pivot support

Goal:

> MentorAI becomes a continuous mentor rather than a one-time generator.

---

## Phase 6 — Job Intelligence

Add:

- Job description decoder
- Job matching
- Job readiness
- Personalized job recommendations

Goal:

> Connect learning directly to actual employment opportunities.

---

# 40. Quality Principles

MentorAI must prioritize:

### Accuracy over confident answers

If data is insufficient, say so.

### Evidence over AI assumptions

Use collected data whenever possible.

### Personalization over generic roadmaps

Recommendations should depend on the student.

### Prioritization over information overload

Tell students what to learn first.

### Adaptation over static plans

Roadmaps should change when circumstances change.

### Explainability over black-box scores

Explain why recommendations were made.

---

# 41. Important Product Differentiators

MentorAI should differentiate itself through:

1. **Career Reality Check**
2. **Reality vs Hype**
3. **Evidence-backed market intelligence**
4. **Personalized skill-gap analysis**
5. **"What should I NOT learn yet?"**
6. **Adaptive roadmap**
7. **Job-readiness analysis**
8. **Job-description decoder**
9. **Career path simulator**
10. **Career pivot support**
11. **Continuous mentorship**

Do not attempt to build all differentiators in the MVP.

---

# 42. Coding Principles for Codex

When implementing the project:

- Prefer clean, maintainable Java.
- Follow Spring Boot conventions.
- Use layered architecture.
- Use DTOs instead of exposing entities directly through APIs.
- Use validation.
- Use meaningful exception handling.
- Use interfaces where they improve testability.
- Avoid premature abstractions.
- Avoid unnecessary microservices.
- Keep the application modular but start as a modular monolith.
- Write tests for important business logic.
- Keep AI prompts versioned in code/resources.
- Use structured AI outputs instead of parsing arbitrary text where possible.
- Keep external integrations behind services/interfaces.
- Never hardcode credentials.
- Use environment variables.
- Add logging around market-data collection and AI workflows.
- Make market-data collection idempotent where possible.
- Store source URLs and timestamps for collected data.
- Track provenance for market claims.
- Never fabricate market statistics.

---

# 43. Architecture Philosophy

Start as a:

> **Modular Monolith**

NOT microservices.

Suggested modules:

```text
auth
profile
career
market
skills
roadmap
progress
jobs
mentor
ai
common
```

Keep module boundaries clean.

If the project eventually becomes large enough, modules can be separated later.

---

# 44. GitHub Repository Structure

Suggested structure:

```text
mentorai/
│
├── README.md
├── CONTEXT.md
├── LICENSE
├── .gitignore
├── docker-compose.yml
│
├── backend/
│   ├── pom.xml
│   └── src/
│
├── frontend/
│   ├── package.json
│   └── src/
│
├── docs/
│   ├── architecture/
│   ├── api/
│   ├── database/
│   └── decisions/
│
└── scripts/
```

---

# 45. Docker Development Environment

Eventually use Docker Compose for:

```text
mentorai-frontend
mentorai-backend
postgres
ollama
```

Development should still work without Docker where practical.

Do not containerize unnecessarily during the first few hours of development.

---

# 46. Future Features

Potential future additions:

- Resume analyzer
- LinkedIn profile analyzer
- Portfolio analyzer
- GitHub profile analyzer
- Interview preparation
- DSA preparation based on target roles
- Personalized learning resource recommendation
- Certification recommendations
- Internship tracker
- Application tracker
- Automated weekly market report
- Market trend alerts
- Skill trend visualization
- Mentor memory
- Voice mentor
- Mobile application
- Community features

These are NOT part of the initial MVP.

---

# 47. Non-Goals

MentorAI should NOT initially attempt to:

- Guarantee employment
- Guarantee salary
- Guarantee a career outcome
- Automatically apply to jobs
- Replace human career counselors
- Make high-stakes life decisions for users
- Present uncertain market predictions as facts
- Scrape websites in violation of their terms
- Build a giant autonomous agent system before the core product works

---

# 48. Success Criteria

The MVP should be considered successful if a first-year BCA student can:

1. Create a profile.
2. Explain their interests and goals.
3. Receive several realistic career options.
4. Understand the pros/cons and realities of each.
5. See evidence about the relevant market.
6. Choose a path.
7. See exactly what skills they need.
8. Understand what to learn first.
9. Get a personalized roadmap.
10. Track progress.
11. Ask MentorAI questions about their path.
12. Receive an updated roadmap as they progress.

The key question is:

> **Does MentorAI reduce the uncertainty a first-year student has about what they should do next?**

If yes, the product is succeeding.

---

# 49. Core Principle

The entire project can be summarized as:

```text
                 "Don't just tell me
                  what to learn.

                  Tell me WHY,
                  show me the MARKET,
                  understand ME,
                  tell me what matters NOW,
                  and keep adjusting
                  as I progress."
```

MentorAI should feel like a **long-term career navigator**, not a generic AI chatbot.

---

# 50. Instructions for Codex

When working on MentorAI:

1. Read this CONTEXT.md before making architectural decisions.
2. Preserve the Java + Spring Boot primary backend.
3. Prefer Spring AI for LLM integration.
4. Prefer Ollama/local open-source models for development.
5. Prefer PostgreSQL + pgvector rather than introducing another database without a clear reason.
6. Prefer open-source/free libraries.
7. Avoid paid APIs unless explicitly requested.
8. Avoid Python unless there is a concrete technical reason that cannot reasonably be handled in Java.
9. Do not introduce microservices prematurely.
10. Build the MVP incrementally.
11. Do not implement future features unless requested.
12. Keep business logic testable.
13. Keep external integrations replaceable.
14. Do not fabricate market data.
15. Store data provenance where possible.
16. Explain significant architectural decisions in documentation.
17. If a requirement is ambiguous, inspect the existing project structure and documentation before creating new architecture.
18. Prefer modifying existing code over creating duplicate implementations.
19. Do not silently replace technologies already established in the project.
20. Keep the project understandable to a BCA student while maintaining professional engineering standards.

---

# Final Product Definition

**MentorAI is an AI-powered career navigation and adaptive mentorship platform for students.**

It combines:

```text
Student Profile
+
Career Goals
+
Interests
+
Current Skills
+
Real Market Data
+
Job Requirements
+
AI Reasoning
+
Progress Tracking
```

to produce:

```text
Career Options
+
Career Reality Check
+
Market Intelligence
+
Skill Gap
+
Personalized Roadmap
+
Projects
+
Job Readiness
+
Job Matching
+
Continuous Adaptation
```

The long-term goal is for a student to go from:

> **"I don't know what I should do."**

to:

> **"I understand my options, I know why I'm choosing this path, I know what the market expects, I know what to learn next, and MentorAI will help me adapt as I progress."**
