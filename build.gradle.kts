import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform") version "1.9.23"
    id("org.jetbrains.dokka") version "1.9.0"
}

group = "me.c3"
version = "0.1.10"

val doodleVersion: String by project

repositories {
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

    jvm{
        withJava()
        jvmToolchain(11)
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("reflect"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

                // Doodle
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
            }
        }

        val wasmJsMain by getting {
            dependencies {

            }
        }

        val jvmMain by getting{
            dependencies {
                implementation("com.formdev:flatlaf:3.4")
                implementation("com.formdev:flatlaf-extras:3.4")

                // https://mvnrepository.com/artifact/org.apache.xmlgraphics/batik-swing
                implementation("org.apache.xmlgraphics:batik-swing:1.7")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")

            }
        }
    }
}


