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
                implementation("com.zegreatrob.coupling.libraries:model")
                implementation("com.zegreatrob.coupling.libraries:action")
                implementation("com.zegreatrob.coupling.libraries:repository-core")
                implementation("com.zegreatrob.coupling.libraries:json")
                implementation("org.jetbrains.kotlin:kotlin-stdlib")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
                implementation("io.ktor:ktor-client-core:1.6.7")
                implementation("io.ktor:ktor-client-serialization:1.6.7")
                implementation("io.ktor:ktor-client-logging:1.6.7")
                implementation("io.ktor:ktor-client-websockets:1.6.7")
                implementation("com.soywiz.korlibs.klock:klock:2.6.2")
                implementation("io.github.microutils:kotlin-logging:2.1.21")
            }
        }
        val commonTest by getting {
            resources.srcDirs(commonMain.resources.srcDirs)

            dependencies {
                implementation("com.zegreatrob.coupling.libraries:repository-validation")
                implementation("com.zegreatrob.coupling.libraries:test-logging")
                implementation("com.zegreatrob.coupling.libraries:stub-model")
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("com.benasher44:uuid:0.4.0")
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:async")
                implementation("com.zegreatrob.testmints:minassert")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
            }
        }

    }
}

tasks {
    "jsNodeTest" {
        dependsOn(":composeUp")
    }
}
