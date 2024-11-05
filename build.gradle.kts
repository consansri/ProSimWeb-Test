import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl


plugins {
    kotlin("multiplatform") version "2.0.20"
    id("org.jetbrains.dokka") version "1.9.20"
    kotlin("plugin.compose") version "2.0.20"
    id("org.jetbrains.compose") version "1.6.11"
    kotlin("plugin.serialization") version "2.0.20"
    distribution
}


val DIST_NAME: String by project
val DIST_VERSION: String by project
val DIST_YEAR: String by project
val DIST_DEV: String by project
val DIST_ORG: String by project

// execute when config was changed
val buildConfigGenerator by tasks.registering(Sync::class){
    from(
        resources.text.fromString(
            """
                package config
                
                object BuildConfig {
                    const val VERSION = $DIST_VERSION
                    const val NAME = $DIST_NAME
                    const val YEAR = $DIST_YEAR
                    const val DEV = $DIST_DEV
                    const val ORG = $DIST_ORG
                }
                
            """.trimIndent()
        )
    ){
        rename{"BuildConfig.kt"}
        into("config")
    }
    into(layout.buildDirectory.dir("generated-src/kotlin/"))
}

group = "prosim"
version = DIST_VERSION

val dokkaVersion = "1.9.20"
val kotlinVersion = "2.0.20"
val composeVersion = "1.6.11"
val coroutineVersion = "1.9.0-RC.2"
val datetimeVersion = "0.6.1"
val serializationVersion = "1.7.2"


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
            this.mainClass.set("prosim.AppKt")
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

            kotlin.srcDirs(layout.buildDirectory.dir("generated-src/kotlin/"))

            buildConfigGenerator.map { it.destinationDir } // convert the task to a file-provider

            dependencies {
                //implementation(kotlin("reflect"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$datetimeVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

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

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutineVersion")
            }
        }
    }
}

compose.resources {
    publicResClass = true
    //packageOfResClass = "prosim.resources"
    generateResClass = always
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

val copyDistZipToJsDistribution by tasks.registering(Copy::class) {
    group = "distribution"
    description = "Copy distZip output to js distribution folder"
    dependsOn("distZip", "distTar")
    from("build/distributions")
    into("build/dist/js/productionExecutable")
}

val copyComposeDesktopJarToWeb by tasks.registering(Copy::class){
    group = "distribution"
    description = "Copy composeDesktop JAR to composeWeb distribution resources"

    val desktopJar by tasks.named("composeDesktopJar")
    dependsOn(desktopJar)

    from(desktopJar)
    into("build/dist/composeWeb/productionExecutable")
    rename { "$DIST_NAME-$DIST_VERSION.jar" }
}

tasks.named("composeWebBrowserDistribution").configure {
    dependsOn(copyComposeDesktopJarToWeb)
}

//tasks.getByName("jsBrowserDistribution").dependsOn(copyDistZipToJsDistribution)