plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    kotlin("plugin.serialization")
}

kotlin {
    targets {
        js {
            nodejs { testTask { useMocha { timeout = "10s" } } }

            val main = compilations.findByName("main")!!
            val test = compilations.findByName("test")!!
            test.defaultSourceSet.dependsOn(main.defaultSourceSet)
        }
        jvm()
    }
}

dependencies {
    "commonMainImplementation"(project(":coupling-libraries:action"))
    "commonMainImplementation"(project(":coupling-libraries:json"))
    "commonMainImplementation"(project(":coupling-libraries:model"))
    "commonMainImplementation"(project(":coupling-libraries:repository-core"))
    "commonMainImplementation"("com.soywiz.korlibs.klock:klock")
    "commonMainImplementation"("io.ktor:ktor-client-content-negotiation")
    "commonMainImplementation"("io.ktor:ktor-client-core")
    "commonMainImplementation"("io.ktor:ktor-client-logging")
    "commonMainImplementation"("io.ktor:ktor-client-websockets")
    "commonMainImplementation"("io.ktor:ktor-serialization-kotlinx-json")
    "commonMainImplementation"("org.jetbrains.kotlin:kotlin-stdlib")
    "commonMainImplementation"("org.jetbrains.kotlin:kotlin-stdlib-common")
    "commonMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    "commonMainImplementation"("org.jetbrains.kotlinx:kotlinx-serialization-json")

    "commonTestImplementation"(project(":coupling-libraries:repository-validation"))
    "commonTestImplementation"(project(":coupling-libraries:stub-model"))
    "commonTestImplementation"(project(":coupling-libraries:test-logging"))
    "commonTestImplementation"("com.benasher44:uuid")
    "commonTestImplementation"("com.zegreatrob.testmints:async")
    "commonTestImplementation"("com.zegreatrob.testmints:minassert")
    "commonTestImplementation"("com.zegreatrob.testmints:standard")
    "commonTestImplementation"("org.jetbrains.kotlin:kotlin-test")

    "jsMainImplementation"("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    "jvmTestImplementation"("io.ktor:ktor-client-java")
}

tasks {
    "jsNodeTest" {
        dependsOn(":composeUp")
        outputs.cacheIf { true }
    }
}
