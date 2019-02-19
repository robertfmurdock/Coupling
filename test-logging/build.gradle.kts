import com.zegreatrob.coupling.build.BuildConstants
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

repositories {
    mavenCentral()
    maven { url = uri("https://dl.bintray.com/soywiz/soywiz") }
}

kotlin {
    targets {
        add(presets["js"].createTarget("js"))
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(project(":logging"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("io.github.microutils:kotlin-logging-common:1.6.25")
                implementation("com.soywiz:klock:1.1.1")
            }
        }

        getByName("jsMain") {
            dependencies {
                implementation("io.github.microutils:kotlin-logging-js:1.6.25")
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
