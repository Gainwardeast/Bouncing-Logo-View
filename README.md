# Bouncing Logo View

Демонстрационное Android-приложение: летающий логотип DVD-плексер в стиле «classic bouncing logo». Проект показывает **4 разных способа реализации анимации** на Android — от простого `View` до игрового цикла на `SurfaceView` с собственным рендер-потоком.

## ✨ Возможности

- Логотип летает по экрану и отскакивает от краёв (DVD-screensaver)
- Смена цвета при каждом отскоке от стены
- Касание экрана телепортирует логотип в точку нажатия и меняет направление движения
- Физика с учётом **deltaTime** — скорость анимации одинакова на любых FPS и устройствах

## 🧪 Четыре реализации

| Класс | Механизм | Особенности |
|---|---|---|
| `BouncingLogoView` | `View` + `ValueAnimator` + `invalidate()` | Классический путь: рисование через `onDraw`, всё на UI-потоке. Простейший вариант |
| `BouncingLogoThreadSurfaceView` | `SurfaceView` + фоновый `Thread` | Игровой цикл с ручным ограничением до ~60 FPS через `Thread.sleep` |
| `BouncingLogoNativeSurfaceView` | `SurfaceView` + `Choreographer` на **главном** потоке | Кадры синхронизируются с VSync системы — «дышит» вместе с частотой экрана |
| `BouncingLogoNativeSurfaceViewWithThread` | `SurfaceView` + `Choreographer` во **фоновом** потоке со своим `Looper` | Самый «хардкорный» вариант: минимальная нагрузка на UI-поток, корректное завершение через `Looper.quit()` |

Активная реализация выбирается в разметке `main.xml` — подставьте нужный класс в layout.

## 🛠 Технологии

- **Kotlin**, нативный Android SDK (без Compose — классический View-подход)
- `Canvas` / `Paint` — отрисовка текста
- `SurfaceHolder.Callback` — управление жизненным циклом Surface
- `Choreographer` — синхронизация кадров с VSync

## 🏗 Структура проекта

```
app/src/main/java/com/example/bouncingview/
├── ui/
│   ├── BouncingLogoView.kt                        # View + ValueAnimator
│   ├── BouncingLogoThreadSurfaceView.kt           # SurfaceView + Thread
│   ├── BouncingLogoNativeSurfaceView.kt           # SurfaceView + Choreographer (main)
│   └── BouncingLogoNativeSurfaceViewWithThread.kt # SurfaceView + Choreographer (background Looper)
├── MainActivity.kt
└── ui/Theme.kt                                    # тема приложения
```

## 🚀 Сборка и запуск

**Требования:**
- Android Studio
- JDK 11+
- Минимальная версия Android: **7.0 (API 24)**, целевая: **Android 15 (API 35)**

```bash
git clone https://github.com/Gainwardeast/Bouncing-Logo-View.git
cd Bouncing-Logo-View
./gradlew installDebug   # или открыть проект в Android Studio и нажать Run
```

## 🎓 Что показывает проект

Проект полезен как учебный пример для понимания:

1. Чем `View.onDraw` отличается от рисования в `SurfaceView`
2. Как устроен игровой цикл и зачем нужен deltaTime вместо фиксированного шага
3. Как `Choreographer` связывает кадры с частотой обновления экрана
4. Как запустить `Choreographer` на фоновом потоке, создав собственный `Looper`

## 📄 Лицензия

Пока не определена. (Добавьте LICENSE, если хотите опубликовать проект как open source.)
