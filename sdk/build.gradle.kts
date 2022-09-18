plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    kotlin("plugin.serialization")
}

kotlin {
    targets {
        js {
            nodejs { testTask { useMocha { timeout = "10s" } } }
        }
    }

    sourceSets {
        named("commonTest") {
            resources.srcDirs(commonMain.map { it.resources.srcDirs })
        }
    }
}

dependencies {
    "commonMainImplementation"(project(":coupling-libraries:model"))
    "commonMainImplementation"(project(":coupling-libraries:action"))
    "commonMainImplementation"(project(":coupling-libraries:repository-core"))
    "commonMainImplementation"(project(":coupling-libraries:json"))
    "commonMainImplementation"("org.jetbrains.kotlin:kotlin-stdlib")
    "commonMainImplementation"("org.jetbrains.kotlin:kotlin-stdlib-common")
    "commonMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    "commonMainImplementation"("org.jetbrains.kotlinx:kotlinx-serialization-json")
    "commonMainImplementation"("io.ktor:ktor-client-core")
    "commonMainImplementation"("io.ktor:ktor-serialization-kotlinx-json")
    "commonMainImplementation"("io.ktor:ktor-client-content-negotiation")
    "commonMainImplementation"("io.ktor:ktor-client-logging")
    "commonMainImplementation"("io.ktor:ktor-client-websockets")
    "commonMainImplementation"("com.soywiz.korlibs.klock:klock")

    "commonTestImplementation"(project(":coupling-libraries:repository-validation"))
    "commonTestImplementation"(project(":coupling-libraries:test-logging"))
    "commonTestImplementation"(project(":coupling-libraries:stub-model"))
    "commonTestImplementation"("org.jetbrains.kotlin:kotlin-test")
    "commonTestImplementation"("com.benasher44:uuid")
    "commonTestImplementation"("com.zegreatrob.testmints:standard")
    "commonTestImplementation"("com.zegreatrob.testmints:async")
    "commonTestImplementation"("com.zegreatrob.testmints:minassert")

    "jsMainImplementation"("org.jetbrains.kotlin-wrappers:kotlin-extensions")
}

tasks {
    "jsNodeTest" {
        dependsOn(":composeUp")
        outputs.cacheIf { true }
    }
}
