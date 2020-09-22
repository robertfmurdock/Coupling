import com.zegreatrob.coupling.build.BuildConstants

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("kotlinx-serialization") version "1.4.10"
}

kotlin {
    targets {
        jvm()
        js(BOTH) {
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
                implementation("com.soywiz.korlibs.klock:klock:1.12.0")
                api("io.github.microutils:kotlin-logging:2.0.3")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0-RC2")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
            }
        }
    }
}

tasks {
}
