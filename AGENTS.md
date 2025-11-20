# IMPORTANT NOTES
- Use docser search_android tool to search online documentation about Android/Kotlin for the target urls/hrefs, and use docser crawl_url tool to crawl specific urls for information.
- Always follow the project rules and code style guidelines below.
- Use a simple term or single term or a keyword to search for information when needed at a time, don't overwhelm with too many terms all at once, for example, search for "FloatingActionButtonMenu" or "ToggleFloatingActionButton" or "FloatingActionButtonMenuItem" separetely instead of making a single query of "FloatingActionButtonMenu ToggleFloatingActionButton FloatingActionButtonMenuItem docs"

# BUILD COMMANDS
- Build: `gradlew.bat assembleDebug` (VS Code task: "Assemble Debug APK")
- Format: `java -jar jars\ktfmt-0.59-with-dependencies.jar --kotlinlang-style -r src composeApp\src` (VS Code task: "Format Code")
- Test: `gradlew.bat test` (single test: `gradlew.bat test --tests "ClassName.testName"`)

# FOLDER STRUCTURE
```
composeApp/src/androidMain/kotlin/dev/quanghuy/mpcareal/
├── App.kt                 # Main app entry point
├── MainActivity.kt         # Android activity
├── models/                # Data classes
├── viewmodel/             # ViewModels for state management
├── data/                  # Data sources and sample data
├── components/            # Reusable UI components
├── screens/               # Screen composables
├── navigation/            # Navigation setup
└── Platform.kt           # Platform-specific code
```

# CODE STYLE
- Kotlin Multiplatform with Compose Multiplatform
- Material 3 Experimental APIs required (@OptIn(ExperimentalMaterial3Api::class))
- Package: `dev.quanghuy.mpcareal`
- Target JVM 11, compileSdk/targetSdk from libs.versions.toml
- Use ktfmt with kotlinlang-style for formatting
- Import organization: keep necessary imports, don't remove unrelated ones
- Naming: PascalCase for classes, camelCase for functions/variables
- Error handling: prefer proper fixes over simplification

# PROJECT RULES
- Never recreate entire files when editing - modify systematically
- Ask user confirmation before adding dependencies
- Don't change build configurations or existing dependencies
- Use official Android/Kotlin docs for reference
- When stuck, ask for help via PowerShell prompt `echo 'I need help, can you type in your answer?'; $answer = Read-Host 'Your answer'; Write-Host "Answer received: $answer"`.
