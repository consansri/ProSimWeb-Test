import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

val serialization_version: String by project
val kotlin_version: String by project
val datetime_version: String by project
val coroutine_version: String by project
val compose_version: String by project
val dokka_version: String by project

val DIST_NAME: String by project
val DIST_VERSION: String by project
val DIST_YEAR: String by project
val DIST_DEV: String by project
val DIST_ORG: String by project
val DIST_FILENAME = "$DIST_NAME - $DIST_VERSION"

plugins {
    kotlin("multiplatform") version "2.0.21"
    id("org.jetbrains.dokka") version "1.9.20"
    kotlin("plugin.compose") version "2.0.21"
    id("org.jetbrains.compose") version "1.7.1"
    kotlin("plugin.serialization") version "2.0.21"
    id("com.gradleup.shadow") version "8.3.5"
    distribution
}


// execute when config was changed
val buildConfigGenerator by tasks.registering(Sync::class) {
    from(
        resources.text.fromString(
            """
                package config
                
                object BuildConfig {
                    const val NAME = "$DIST_NAME"
                    const val VERSION = "$DIST_VERSION"
                    const val YEAR = "$DIST_YEAR"
                    const val DEV = "$DIST_DEV"
                    const val ORG = "$DIST_ORG"
                    const val FILENAME = "$DIST_NAME - $DIST_VERSION"
                }
                
            """.trimIndent()
        )
    ) {
        rename { "BuildConfig.kt" }
        into("config")
    }
    into(layout.buildDirectory.dir("generated-src/kotlin/"))
}

group = "prosim"
version = DIST_VERSION



repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    maven {
        url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

kotlin {
    js(IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs("composeWeb") {
        browser()
        binaries.executable()
    }

    jvm("composeDesktop") {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        mainRun {
            mainClass.set("prosim.AppKt")
        }
    }

    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        mainRun {
            mainClass.set("prosim.AppKt")
        }
    }

    sourceSets {
        val commonMain by getting {

            kotlin.srcDirs(layout.buildDirectory.dir("generated-src/kotlin/"))

            buildConfigGenerator.map { it.destinationDir } // convert the task to a file-provider

            dependencies {
                //implementation(kotlin("reflect"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutine_version")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$datetime_version")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization_version")

                // Enables FileKit without Compose dependencies
                implementation("io.github.vinceglb:filekit-core:0.8.7")
                // Enables FileKit with Composable utilities
                implementation("io.github.vinceglb:filekit-compose:0.8.7")

                // Compose
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.components.resources)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val composeWebMain by getting {
            dependencies {

            }
        }

        val composeDesktopMain by getting {
            dependencies {
                // For Compose
                implementation(compose.desktop.currentOs)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.3.1-pre.757")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.3.1-pre.757")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:11.11.4-pre.757")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-tanstack-react-virtual:3.5.1-pre.757")
            }
        }

        val jvmMain by getting {
            dependencies {

                // For Swing
                implementation("com.formdev:flatlaf:3.4")
                implementation("com.formdev:flatlaf-extras:3.4")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutine_version")
            }
        }
    }
}

distributions {
    main {
        distributionBaseName = DIST_FILENAME

        contents {

        }
    }
}

compose.resources {
    publicResClass = true
    //packageOfResClass = "prosim.resources"
    generateResClass = always
}

val composeDesktopFatJar by tasks.register<Jar>("composeDesktopFatJar") {
    group = "build"
    description = "Assembles an executable JAR for the Compose Desktop application"

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "prosim.AppKt"
    }

    // Collect all dependencies and package them into the JAR
    val runtimeClasspath = configurations["composeDesktopRuntimeClasspath"]
    from(runtimeClasspath.map { if (it.isDirectory) it else zipTree(it) })

    // Include all files from the main output
    with(tasks.getByName("composeDesktopJar") as CopySpec)
}

val copyDistZipToJsDistribution by tasks.registering(Copy::class) {
    group = "distribution"
    description = "Copy distZip output to js distribution folder"
    dependsOn("distZip", "distTar")
    from("build/distributions")
    into("build/dist/js/productionExecutable")
}

val copyComposeDesktopJarToWeb by tasks.registering(Copy::class) {
    group = "distribution"
    description = "Copy composeDesktop JAR to composeWeb distribution resources"

    dependsOn(composeDesktopFatJar)

    from(composeDesktopFatJar)
    into("build/dist/composeWeb/productionExecutable")
    rename { "$DIST_FILENAME.jar" }
}

tasks.named("composeWebBrowserDistribution").configure {
    dependsOn(copyComposeDesktopJarToWeb)
}

//tasks.getByName("jsBrowserDistribution").dependsOn(copyDistZipToJsDistribution)