import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform") version "2.0.0"
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

val osName = System.getProperty("os.name")
val hostOs = when {
    osName == "Mac OS X" -> "macos"
    osName.startsWith("Win") -> "windows"
    osName.startsWith("Linux") -> "linux"
    else -> error("Unsupported OS: $osName")
}

val osArch = System.getProperty("os.arch")
var hostArch = when (osArch) {
    "x86_64", "amd64" -> "x64"
    "aarch64" -> "arm64"
    else -> error("Unsupported arch: $osArch")
}

val host = "${hostOs}-${hostArch}"

var version = "0.0.0-SNAPSHOT"
if (project.hasProperty("skiko.version")) {
    version = project.properties["skiko.version"] as String
}

val resourcesDir = "$buildDir/resources"
val skikoWasm by configurations.creating

val isCompositeBuild = extra.properties.getOrDefault("skiko.composite.build", "") == "1"

val unzipTask = tasks.register("unzipWasm", Copy::class) {
    destinationDir = file(resourcesDir)
    from(skikoWasm.map { zipTree(it) })

    if (isCompositeBuild) {
        val skikoWasmJarTask = gradle.includedBuild("skiko").task(":skikoWasmJar")
        dependsOn(skikoWasmJarTask)
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

    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        mainRun {
            this.mainClass.set("prosim.AppKt")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.skiko:skiko:$version")

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
            dependsOn(commonMain)
            dependencies {
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.3.1-pre.757")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.3.1-pre.757")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:11.11.4-pre.757")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-tanstack-react-virtual:3.5.1-pre.757")
            }
        }

        val wasmJsMain by getting {
            dependencies {

            }
        }

        val jvmMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation("com.formdev:flatlaf:3.4")
                implementation("com.formdev:flatlaf-extras:3.4")

                implementation("org.jetbrains.skiko:skiko-awt-runtime-$hostOs-$hostArch:$version")  // replace with the latest version
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
    dependsOn("distZip")
    from("build/distributions")
    into("build/dist/js/productionExecutable")
}

tasks.getByName("jsBrowserDistribution").dependsOn(copyDistZipToJsDistribution)