<div align="center">

# SoulSync — Android
### Native emotional wellness OS for Android. Offline-first.

![Status](https://img.shields.io/badge/status-in%20development-F59E0B)
![Platform](https://img.shields.io/badge/platform-Android-22C55E)
![Language](https://img.shields.io/badge/language-Kotlin-6366F1)
![UI](https://img.shields.io/badge/ui-Jetpack%20Compose-22D3EE)
![Architecture](https://img.shields.io/badge/arch-MVVM-525252)

</div>

---

## Overview

SoulSync Android is the native Android version of SoulSync, rebuilt from scratch in Kotlin and Jetpack Compose — not a WebView wrapper, a true native app with native performance and native UX patterns. We chose an offline-first architecture using Room Database with local AES encryption, meaning the app works completely without internet and syncs when connected. The interface is inspired by Apple Journal and Apple Health, utilizing the Material 3 design language adapted to feel calm, intentional, and distraction-free.

---

## Features

- **Offline-first journaling** — write entries without internet, data syncs automatically when connection is available
- **Local encryption** — journal data encrypted locally using AES before storage
- **Gemini AI reflections** — personalized AI responses to journal entries (in progress)
- **Mood tracking** — daily mood logging with historical view
- **Material 3 UI** — clean, calm interface following Android's latest design system
- **Theme system** — multiple visual themes including dark mode variants
- **Achievements** — milestone tracking across journaling and wellness habits
- **Cycle tracking** — planned for upcoming release

---

## Tech Stack

| Technology | Role |
|------------|------|
| Kotlin | Primary language |
| Jetpack Compose | Declarative UI framework |
| Room Database | Local offline data storage |
| AES Encryption | Local data security |
| Google Gemini AI | AI journal reflections |
| MVVM | Architecture pattern |
| Hilt DI | Dependency injection |
| WorkManager | Background sync scheduling |
| Kotlin Coroutines | Async operations |
| Kotlin Flow | Reactive state management |
| Material 3 | Design system |
| Android SDK | Platform |

---

## Architecture

- MVVM architecture separates UI (Compose screens), business logic (ViewModels), and data (Repository + Room) — each layer communicates through Kotlin Flow streams
- Hilt handles dependency injection across the entire app — ViewModels, repositories, and database instances are all injected, making the codebase testable and modular
- Room Database is the single source of truth for all user data — Supabase sync is a secondary layer, not a dependency for core functionality
- Local AES encryption wraps sensitive journal content before it is written to Room, so even direct database access cannot read plaintext entries
- WorkManager schedules background sync jobs that run when network is available, ensuring offline entries are uploaded without user intervention

---

## Challenges Being Solved

1. **Offline-first Room database architecture**
   Most apps treat the cloud as the source of truth and local storage as a cache. SoulSync Android inverts this — Room is the source of truth and Supabase is the backup. This requires a sync strategy that handles conflicts, ordering, and partial sync states gracefully.

2. **Local AES encryption**
   Encrypting data at rest in an Android app requires careful key management — the key must be stored securely (Android Keystore) and the encryption must be fast enough to not block the UI thread during read/write operations.

3. **Gemini AI on Android**
   Running Gemini AI calls from an Android app requires handling network latency, streaming responses, error states, and rate limits in a way that keeps the UI responsive and the user experience smooth.

4. **MVVM with Kotlin Coroutines and Flow**
   Reactive data flow from Room through a Repository into a ViewModel and into a Compose screen requires careful lifecycle management to avoid memory leaks and ensure UI state survives configuration changes.

---

## Web Version

The SoulSync web app is live and fully featured.

🔗 **[soulsync.lovable.app](https://soulsync.lovable.app)**<br>
📂 **[Web Repository](https://github.com/Pro-Prince/yoursoulsync)**

The Android version mirrors the web app's core features with native performance, offline capability, and platform-specific UX patterns.

---

## Local Development

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 26+
- A Google Gemini API key
- A Supabase project (optional for offline testing)

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

