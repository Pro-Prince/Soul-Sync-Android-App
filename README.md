<div align="center">

# Soul Sync — Android
### Your personal AI-powered emotional wellness sanctuary.

![Status](https://img.shields.io/badge/status-live-22C55E?style=flat-square)
![Platform](https://img.shields.io/badge/platform-Android-6366F1?style=flat-square)
![Language](https://img.shields.io/badge/language-Kotlin-22D3EE?style=flat-square)
![UI](https://img.shields.io/badge/ui-Jetpack%20Compose-525252?style=flat-square)
![License](https://img.shields.io/badge/license-MIT-F59E0B?style=flat-square)

</div>

---

## Overview

Soul Sync Android is a modern AI-powered emotional wellness application that transforms journaling into a deeply personal, supportive, and reflective experience. Instead of being just another digital diary, it combines private journaling, AI emotional reflection, mood tracking, cycle awareness, personal analytics, achievements, and secure cloud synchronization into one calming sanctuary. Built as a true native Android app in Kotlin and Jetpack Compose — not a WebView wrapper — with an offline-first architecture so the app works fully without a connection and syncs the moment one is available.

---

## Features

- **AI Emotional Reflection Coach** — journal entries are analyzed for emotional tone and returned with personalized, supportive responses, reflection summaries, and gentle next-step questions rather than generic output
- **Private Digital Journal** — a distraction-free writing canvas with title and body support, draft preservation, media attachments, and a Zen writing mode
- **Mood Tracking & Emotional Analytics** — daily mood logging with weekly, monthly, and yearly trend analysis, converted into narrative-based insights instead of raw numbers
- **Period Tracker & Emotional Support** — private cycle tracking with history, predictions, and emotional pattern correlation across cycle phases
- **Memory Scrapbook** — a chronological personal timeline with photo and attachment support for preserving meaningful moments
- **Achievements & Growth System** — milestone and consistency tracking that celebrates reflection habits without creating pressure
- **Secure Authentication & Cloud Sync** — email/password and Google auth architecture via Supabase, with user-isolated data and protected cloud synchronization

---

## Tech Stack

| Technology | Role |
|---|---|
| Kotlin | Primary language |
| Jetpack Compose | Declarative UI framework |
| Material Design | Visual design system |
| MVVM | Architecture pattern |
| Repository Pattern | Data layer abstraction |
| Dependency Injection | Modular, testable architecture |
| Room Database | Local offline-first storage |
| Android DataStore | Preferences and lightweight state |
| Supabase | Backend, auth, database, sync |
| PostgreSQL | Cloud data storage |
| AI Reflection Engine | Personalized emotional responses |

---

## Architecture

- MVVM architecture separates UI (Compose screens), business logic (ViewModels), and data (Repository + Room) with reactive state management flowing cleanly between layers
- Room Database and Android DataStore power an offline-first design — journaling, mood logging, and cycle tracking all work fully without a network connection
- Supabase handles authentication and cloud synchronization as a secondary layer on top of local storage, so cloud sync never blocks the core experience
- Dependency injection keeps ViewModels, repositories, and data sources modular and independently testable
- The AI reflection engine analyzes journal content and returns structured, calm, editorial-style responses focused on understanding rather than judgment

---

## Challenges Solved

**1. Turning journal entries into meaningful emotional insight**
People write journals but struggle to understand their own emotional patterns. Built an AI-powered reflection engine that analyzes written entries and returns personalized, supportive responses instead of generic sentiment output.

**2. Calm design in a category full of clinical apps**
Most wellness apps feel clinical or overwhelming. Designed a calm, premium experience — spacious layouts, warm colors, elegant typography — inspired by premium wellness products rather than technical dashboards.

**3. Privacy for deeply personal data**
Emotional and cycle data requires strong privacy protection. Built secure authentication with user-isolated data architecture, ensuring every user's emotional data connects only to their own account.

**4. Motivation without pressure**
Traditional habit trackers cause users to lose motivation. Built an achievement system that celebrates consistency and personal growth milestones without gamifying reflection into a chore.

**5. Numbers without meaning**
Raw mood-tracking numbers rarely mean anything to the person looking at them. Built narrative-based analytics that explain emotional patterns in human language across weekly, monthly, and yearly views.

---

## Web Version

The Soul Sync web app is live and fully featured.

🔗 **[soulsync.lovable.app](https://soulsync.lovable.app)**
📂 **[Web Repository](https://github.com/Pro-Prince/yoursoulsync)**

The Android version mirrors the web app's core philosophy with native performance, full offline capability, and platform-specific UX patterns.

---

## Author

**Prince Patel** — CS Student & Builder

- 🌐 Portfolio: [prince-patel-portfolio.vercel.app](https://prince-patel-portfolio.vercel.app)
- 🐙 GitHub: [github.com/Pro-Prince](https://github.com/Pro-Prince)
- 𝕏 X: [@Pro_Prince_1](https://x.com/Pro_Prince_1)
- 💼 LinkedIn: [linkedin.com/in/prince-patel476](https://www.linkedin.com/in/prince-patel476/)

---

## License

This project is licensed under the MIT License.
