plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    kotlin("plugin.serialization")
}
kotlin {
    js {
        nodejs()
        useCommonJs()
    }
    jvm()
}

dependencies {
    commonMainImplementation(project(":libraries:action"))
    commonMainImplementation(project(":libraries:test-logging"))
    commonMainApi("com.zegreatrob.testmints:action")
    commonMainApi("com.zegreatrob.testmints:action-async")
    commonMainApi("com.zegreatrob.testmints:async")
    commonMainApi("com.zegreatrob.testmints:standard")
    commonMainApi("com.zegreatrob.testmints:minassert")
    commonMainImplementation("org.jetbrains.kotlin:kotlin-test")
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-core")

    "jvmMainImplementation"(kotlin("reflect"))
    "jvmMainImplementation"("org.slf4j:slf4j-simple")
    "jvmMainImplementation"("org.junit.jupiter:junit-jupiter-api")
    "jvmMainImplementation"("org.junit.jupiter:junit-jupiter-engine")
    "jvmMainImplementation"("com.fasterxml.jackson.core:jackson-databind")

    "jsMainImplementation"("org.jetbrains.kotlinx:kotlinx-serialization-core")
}

tasks {
    named<Test>("jvmTest") {
        systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")

        useJUnitPlatform()
    }
}
