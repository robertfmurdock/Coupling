import com.zegreatrob.coupling.build.BuildConstants
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("kotlinx-serialization") version "1.3.41"
}

kotlin {
    targets {
        jvm()
        add(presets["js"].createTarget("js"))
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("io.github.microutils:kotlin-logging-common:1.6.26")
                implementation("com.soywiz:klock:1.1.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:0.10.0")
            }
        }

        getByName("jsMain") {
            dependencies {
                implementation("io.github.microutils:kotlin-logging-js:1.6.26")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.10.0")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
            }
        }
    }
}

tasks {
    getByName<Kotlin2JsCompile>("compileKotlinJs") {
        kotlinOptions.moduleKind = "umd"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }
}
