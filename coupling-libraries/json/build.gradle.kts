plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    kotlin("plugin.serialization")
}
kotlin {
    targets {
        jvm()
        js { nodejs() }
    }
}

dependencies {
    commonMainApi(project(":coupling-libraries:model"))
    commonMainImplementation(kotlin("stdlib"))
    commonMainImplementation(kotlin("stdlib-common"))
    commonMainImplementation("com.soywiz.korlibs.klock:klock")
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    commonMainImplementation("io.ktor:ktor-client-core")
    commonMainImplementation("io.ktor:ktor-serialization-kotlinx-json")
    commonMainImplementation("io.ktor:ktor-client-content-negotiation")
    commonMainImplementation("io.ktor:ktor-client-logging")

    commonTestImplementation(project(":coupling-libraries:test-logging"))
    commonTestImplementation(project(":coupling-libraries:stub-model"))
    commonTestImplementation("com.zegreatrob.testmints:standard")
    commonTestImplementation("com.zegreatrob.testmints:minassert")
    commonTestImplementation("org.jetbrains.kotlin:kotlin-test")

    "jvmMainImplementation"(kotlin("reflect"))

    "jvmTestImplementation"(kotlin("reflect"))
    "jvmTestImplementation"("org.junit.jupiter:junit-jupiter-api")
    "jvmTestImplementation"("org.junit.jupiter:junit-jupiter-engine")

    "jsMainImplementation"(kotlin("stdlib-js"))
}
