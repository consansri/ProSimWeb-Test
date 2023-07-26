plugins {
    kotlin("multiplatform") version "1.9.0"
}

group = "me.c3"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
}

kotlin {
    js {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }

    wasm {
        this.applyBinaryen()
        browser()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.2.0-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.2.0-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:11.9.3-pre.346")

            }
        }

        val wasmMain by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}