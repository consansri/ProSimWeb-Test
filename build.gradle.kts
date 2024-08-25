import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform") version "2.0.20"
    id("org.jetbrains.dokka") version "1.9.20"
    distribution
}

group = "prosim"
version = "0.2.6"

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

    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        mainRun {
            this.mainClass.set("prosim.AppKt")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                //implementation(kotlin("reflect"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
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
                implementation("com.formdev:flatlaf:3.4")
                implementation("com.formdev:flatlaf-extras:3.4")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")
            }
        }
    }
}

distributions {
    main {
        distributionBaseName.set("ProSimJVM")
        contents {
            into("") {
                val jvmJar by tasks.getting
                from(jvmJar)
            }
            into("lib/") {
                val main by kotlin.jvm().compilations.getting
                from(main.runtimeDependencyFiles)
            }
        }
    }
}

tasks.withType<Jar>() {
    doFirst {
        manifest {
            val main by kotlin.jvm().compilations.getting
            attributes(
                "Main-Class" to "prosim.AppKt",
                "Class-Path" to main.runtimeDependencyFiles.files.joinToString(" ") { "lib/" + it.name }
            )
        }
    }
}

val copyDistZipToJsDistribution by tasks.registering(Copy::class){
    group = "distribution"
    description = "Copy distZip output to js distribution folder"
    dependsOn("distZip", "distTar")
    from("build/distributions")
    into("build/dist/js/productionExecutable")
}

tasks.getByName("jsBrowserDistribution").dependsOn(copyDistZipToJsDistribution)