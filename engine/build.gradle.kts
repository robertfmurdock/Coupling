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
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
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
    }
    getByName<Kotlin2JsCompile>("compileTestKotlinJs") {
        kotlinOptions.moduleKind = "commonjs"
    }
    getByName<KotlinJsDce>("runDceJsKotlin") {
        keep("engine.spinContext", "engine.historyFromArray")
    }
}
