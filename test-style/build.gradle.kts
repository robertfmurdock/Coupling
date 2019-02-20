
import com.zegreatrob.coupling.build.BuildConstants
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

repositories {
    mavenCentral()
}

kotlin {
    targets {
        jvm()
        add(presets["js"].createTarget("js"))
        macosX64()
        linuxX64()
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlin:kotlin-test-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.1.1")
            }
        }

        val jvmMain by getting {
            dependencies  {
                implementation("org.jetbrains.kotlin:kotlin-test-junit:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1")
            }
        }

        val macosX64Main by getting {
            dependencies  {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:1.1.1")
            }
        }

        val linuxX64Main by getting {
            dependencies  {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:1.1.1")
            }
        }

        getByName("jsMain") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.1.1")
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
