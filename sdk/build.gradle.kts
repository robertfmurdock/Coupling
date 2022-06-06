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

        val commonMain = named("commonMain") {
            dependencies {
                implementation(project(":coupling-libraries:model"))
                implementation(project(":coupling-libraries:action"))
                implementation(project(":coupling-libraries:repository-core"))
                implementation(project(":coupling-libraries:json"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
                implementation("io.ktor:ktor-client-core:2.0.2")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.0.2")
                implementation("io.ktor:ktor-client-content-negotiation:2.0.2")
                implementation("io.ktor:ktor-client-logging:2.0.2")
                implementation("io.ktor:ktor-client-websockets:2.0.2")
                implementation("com.soywiz.korlibs.klock:klock:2.7.0")
                implementation("io.github.microutils:kotlin-logging:2.1.23")
            }
        }
        named("commonTest") {
            resources.srcDirs(commonMain.get().resources.srcDirs)

            dependencies {
                implementation(project(":coupling-libraries:repository-validation"))
                implementation(project(":coupling-libraries:test-logging"))
                implementation(project(":coupling-libraries:stub-model"))
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("com.benasher44:uuid:0.4.1")
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:async")
                implementation("com.zegreatrob.testmints:minassert")
            }
        }

        named("jsMain") {
            dependencies {
                implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
            }
        }
    }
}

tasks {
    "jsNodeTest" {
        dependsOn(":composeUp")
        outputs.cacheIf { true }
    }
}
