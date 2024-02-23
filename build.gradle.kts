import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform") version "1.9.22"
    id("org.jetbrains.dokka") version "1.9.0"
}

group = "me.c3"
version = "0.1.9"

val doodleVersion: String by project

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
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
    wasmJs {
        browser()
        binaries.executable()

        // Apply binaryen to optimize output
        if (project.gradle.startParameter.taskNames.find { it.contains("wasmJsBrowserProductionWebpack") } != null) {
            applyBinaryen {
                binaryenArgs = mutableListOf(
                    "--enable-nontrapping-float-to-int",
                    "--enable-gc",
                    "--enable-reference-types",
                    "--enable-exception-handling",
                    "--enable-bulk-memory",
                    "--inline-functions-with-loops",
                    "--traps-never-happen",
                    "--fast-math",
                    "--closed-world",
                    "--metrics",
                    "-O3", "--gufa", "--metrics",
                    "-O3", "--gufa", "--metrics",
                    "-O3", "--gufa", "--metrics",
                )
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.nacular.doodle:core:$doodleVersion")
                implementation("io.nacular.doodle:browser:$doodleVersion")

                // Optional
                implementation("io.nacular.doodle:controls:$doodleVersion")
                implementation("io.nacular.doodle:animation:$doodleVersion")
                implementation("io.nacular.doodle:themes:$doodleVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.2.0-pre.607")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.2.0-pre.607")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:11.11.1-pre.607")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.7.3")
            }
        }

        val wasmJsMain by getting {
            dependencies {

            }
        }
    }
}