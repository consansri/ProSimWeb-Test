plugins {
    kotlin("multiplatform") version "1.9.20"
    id("org.jetbrains.dokka") version "1.9.0"
}

group = "me.c3"
version = "0.1.2"

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

    wasmJs {
        binaries.executable()
        browser {

        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {

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