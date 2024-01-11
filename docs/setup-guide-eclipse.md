# Eclipse Setup Guide #

### 1. Install Eclipse Plugins ###

- Buildship Gradle Integration
- Kotlin Plugin for Eclipse
- Enhanced Kotlin for Eclipse (Make sure kotlin build script is activated)

### 2. Import Project ###

1. File &rarr; Open Projects from File System...
2. Select Directory or Archive which the project is copied or cloned at &rarr; Finish
4. Right Click on Project &rarr; Configure &rarr; Add Kotlin Nature (Depending on Gradle Version also add Gradle)

### 3. Open Gradle Task View ###

Window &rarr; Show View &rarr; Other &rarr; Gradle &rarr; Gradle Tasks

## Development ##

Gradle Tasks &rarr; `[Project Name]` &rarr; kotlin browser &rarr; jsBrowserDevelopmentRun

**Disclaimer:**\
Automatic recompilation seems not to work in every eclipse setup! (works in IntelliJ)\
To get better Syntax Highlighting use IntelliJ IDEA!

## Distribution ##

1. Gradle Tasks &rarr; [Project Name] &rarr; kotlin browser &rarr; jsBrowserDistribution
2. The Distributed Web Files (HTML, CSS, JavaScript, ICONS, ...) can be found at\
```[Project Dir]/build/dist/js/productionExecutable``` 


