
import com.zegreatrob.coupling.build.BuildConstants

plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    id("com.zegreatrob.coupling.plugins.serialization")
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
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
            }
        }
        val commonMain by getting {
            dependencies {
                implementation(project(":model"))
                implementation(kotlin("stdlib", BuildConstants.kotlinVersion))
                implementation(kotlin("stdlib-common", BuildConstants.kotlinVersion))
                implementation("com.soywiz.korlibs.klock:klock:2.4.12")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":test-logging"))
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:minassert")
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
                implementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
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
