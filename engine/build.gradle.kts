import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJsDce

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "1.3.11"
}

apply {
    plugin("kotlin-dce-js")
}

repositories {
    mavenCentral()
}

kotlin {
    targets {
        add(presets["js"].createTarget("js"))
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:1.3.11")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.1.0")
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
                implementation("io.kotlintest:kotlintest-runner-junit5:3.1.11")
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
    getByName<Kotlin2JsCompile>("compileTestKotlinJs") {
        kotlinOptions.moduleKind = "commonjs"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }
    getByName<KotlinJsDce>("runDceJsKotlin") {
        keep("engine.spinContext", "engine.historyFromArray")
    }
}
