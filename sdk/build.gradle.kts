import com.zegreatrob.coupling.build.BuildConstants

plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    id("com.zegreatrob.coupling.plugins.serialization")
}

kotlin {
    targets {
        js {
            nodejs { testTask { useMocha { timeout = "10s" } } }
        }
    }

    sourceSets {
        all { languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi") }

        val commonMain by getting {
            dependencies {
                implementation(project(":model"))
                implementation(project(":repository-core"))
                implementation(project(":json"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
                implementation("io.ktor:ktor-client-core:1.6.7")
                implementation("io.ktor:ktor-client-serialization:1.6.7")
                implementation("io.ktor:ktor-client-logging:1.6.7")
                implementation("io.ktor:ktor-client-websockets:1.6.7")
                implementation("com.soywiz.korlibs.klock:klock:2.4.13")
                implementation("io.github.microutils:kotlin-logging:2.1.21")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
            }
        }
        val commonTest by getting {
            resources.srcDirs(commonMain.resources.srcDirs)

            dependencies {
                implementation(project(":repository-validation"))
                implementation(project(":test-logging"))
                implementation(project(":stub-model"))
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("com.benasher44:uuid:0.4.0")
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:async")
                implementation("com.zegreatrob.testmints:minassert")
            }
        }
    }
}

tasks {
    "jsNodeTest" {
        dependsOn(":composeUp")
    }
}
