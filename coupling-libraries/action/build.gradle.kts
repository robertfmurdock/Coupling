plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    kotlin("plugin.serialization")
}

kotlin {

    targets {
        js {
            nodejs { testTask { useMocha { timeout = "10s" } } }
        }
        jvm()
    }
}

tasks {
    named("jvmTest", Test::class) {
        systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
        useJUnitPlatform()
    }
}

dependencies {
    commonMainImplementation(project(":coupling-libraries:model"))
    commonMainImplementation(project(":coupling-libraries:logging"))
    commonMainApi("com.zegreatrob.testmints:action")
    commonMainApi("com.zegreatrob.testmints:action-async")
    commonMainImplementation("org.jetbrains.kotlin:kotlin-stdlib-common")
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    commonMainImplementation("com.benasher44:uuid")
    commonMainImplementation("com.soywiz.korlibs.klock:klock")
    commonMainImplementation("io.github.microutils:kotlin-logging")

    commonTestImplementation(project(":coupling-libraries:json"))
    commonTestImplementation(project(":coupling-libraries:test-logging"))
    commonTestImplementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
    commonTestImplementation("org.jetbrains.kotlin:kotlin-test")
    commonTestImplementation("com.zegreatrob.testmints:standard")
    commonTestImplementation("com.zegreatrob.testmints:minassert")

    "jvmMainImplementation"(kotlin("reflect"))
    "jvmMainImplementation"("io.github.microutils:kotlin-logging")
    "jvmMainImplementation"("com.fasterxml.jackson.core:jackson-databind")

    "jvmTestImplementation"(kotlin("reflect"))
    "jvmTestImplementation"("org.slf4j:slf4j-simple")
    "jvmTestImplementation"("org.junit.jupiter:junit-jupiter-api")
    "jvmTestImplementation"("org.junit.jupiter:junit-jupiter-engine")
}