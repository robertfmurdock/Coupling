
import com.zegreatrob.coupling.build.BuildConstants

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    kotlin("plugin.serialization") version "1.6.0"
}

kotlin {
    targets {
        jvm()
        js {
            useCommonJs()
            nodejs()
        }
    }
    sourceSets {
        all {
            languageSettings {
                useExperimentalAnnotation("kotlinx.serialization.ExperimentalSerializationApi")
            }
        }
        val commonMain by getting {
            dependencies {
                implementation(project(":model"))
                implementation(kotlin("stdlib", BuildConstants.kotlinVersion))
                implementation(kotlin("stdlib-common", BuildConstants.kotlinVersion))
                implementation("com.soywiz.korlibs.klock:klock:2.4.8")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":test-logging"))
                implementation("com.zegreatrob.testmints:standard:5.2.2")
                implementation("com.zegreatrob.testmints:minassert:5.2.2")
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.8.1")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js", BuildConstants.kotlinVersion))
            }
        }
    }
}

tasks {
}
