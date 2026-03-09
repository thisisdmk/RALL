# RALL — Reddit Client for r/all

Современный нативный Android-клиент для просмотра популярного контента с Reddit (r/all), написанный с упором на производительность и адаптивность.

## Стек технологий (Tech Stack)

*   **UI:** Jetpack Compose (1.7.x) — полностью декларативный интерфейс.
*   **Архитектура:** Clean Architecture + MVVM.
*   **DI:** Hilt (Dagger) — инъекция зависимостей.
*   **Network:** Retrofit 2 + Kotlinx Serialization (JSON).
*   **Asynchronous:** Kotlin Coroutines & Flow.
*   **Image Loading:** Landscapist Fresco (оптимизированная работа с памятью).
*   **Video Support:** Media3 ExoPlayer (для воспроизведения видео и гифок в ленте).
*   **Pagination:** Paging 3 (бесконечная лента с кэшированием).
*   **Local Storage:** Jetpack DataStore (настройки и предпочтения).

## Ключевые фичи (Features)

*   **Adaptive Layout:** Использование `WindowSizeClass` для подбора оптимального разрешения изображений и адаптации UI под разные экраны (смартфоны, планшеты).
*   **Modern Build System:** Использование `libs.versions.toml` (Version Catalog) для управления зависимостями.
*   **Kotlin 2.0:** Проект переведен на последнюю версию компилятора Kotlin.
*   **Media Handling:** Интеграция ExoPlayer для бесшовного просмотра медиаконтента.
