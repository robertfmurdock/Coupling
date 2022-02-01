import com.zegreatrob.coupling.build.BuildConstants

plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    id("com.zegreatrob.coupling.plugins.serialization")
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
            languageSettings.optIn("kotlin.Experimental")
        }
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("com.soywiz.korlibs.klock:klock:2.4.13")
                api("io.github.microutils:kotlin-logging:2.1.21")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
            }
        }
    }
}

tasks {
}
