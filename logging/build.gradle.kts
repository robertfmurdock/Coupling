import com.zegreatrob.coupling.build.BuildConstants

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("kotlinx-serialization") version "1.4.10"
}

kotlin {
    targets {
        jvm()
        js {
            nodejs()
            useCommonJs()
        }
    }

    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
        }
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("io.github.microutils:kotlin-logging-common:1.11.5")
                implementation("com.soywiz.korlibs.klock:klock:1.12.0")
            }
        }

        val jsMain by getting {
            dependencies {
                api("io.github.microutils:kotlin-logging-js:1.11.5")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
            }
        }
    }
}

tasks {
}
