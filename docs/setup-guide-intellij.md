# IntelliJ Setup Guide #

### 1. Open Project with IntelliJ IDEA ###

## Development ##

Gradle Tasks &rarr; `[Project Name]` &rarr; kotlin browser &rarr; composeWebBrowserDevelopmentRun

**optional:** change `composeWebBrowserDevelopmentRun` to `composeWebBrowserDevelopmentRun --continuous` for incremental compilation

## Distribution ##

1. Gradle Tasks &rarr; [Project Name] &rarr; Tasks &rarr; kotlin browser &rarr; composeWebBrowserDistribution
2. The Distributed Web Files (HTML, CSS, JavaScript, ICONS, ...) can be found at\
   ```[Project Dir]/build/dist/composeWeb/productionExecutable``` 
