repositories {
    mavenCentral()
}

plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    id("com.zegreatrob.coupling.plugins.serialization")
}

group = "com.zegreatrob.coupling.libraries"

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
            languageSettings.optIn("kotlin.Experimental")
        }
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                implementation("com.soywiz.korlibs.klock:klock:2.7.0")
                api("io.github.microutils:kotlin-logging:2.1.21")
                api("org.jetbrains.kotlinx:kotlinx-serialization-core")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(kotlin("serialization"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
            }
        }
    }
}

tasks {
}
