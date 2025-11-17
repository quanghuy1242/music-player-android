# RULES

1. Always run tasks "Assemble Debug APK" to build and verify the Android application.
2. Before ending a response, run task "Format Code" to ensure all Kotlin code is properly formatted.
3. When you struggle to edit or replace or modify Kotlin code, don't ever recreate the whole file, instead fix or modify systematically. And if you are still struggling, ask user for help by running command `echo 'I need help, can you type in your answer?'; $answer = Read-Host 'Your answer'; Write-Host "Answer received: $answer"`.
4. Beware of adding or modifying import statements in Kotlin files. Always ensure that import statements are necessary and relevant to the code in the file, don't remove any unrelated import statements.
5. When you want to add new dependencies to the project, always ask the user for confirmation first by running command `echo "Do you want to add new dependencies? If yes, please specify which ones."; $answer = Read-Host 'Your answer'; Write-Host "Answer received: $answer"`.
6. The project is runnable and buildable, don't change any existing build configurations and dependencies.
7. Always ensure that any Kotlin code you add or modify adheres to best practices and coding standards.
8. Use fetch tool to look for any information you need about Kotlin Multiplatform and Android development from https://developer.android.com/s/results?q=your_query and https://kotlinlang.org/docs/multiplatform.html?q=your_query&s=full, don't use any other sources.
9. This project uses Material 3 Experimental APIs, so ensure that any UI components you add or modify are compatible with Material 3.
10. When you struggle to fix build errors, don't simplify or skip the implementation, instead try to fix them properly. If you are still struggling, ask user for help by running command `echo 'I need help, can you type in your answer?'; $answer = Read-Host 'Your answer'; Write-Host "Answer received: $answer"`.
---
