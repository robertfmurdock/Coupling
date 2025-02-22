plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}
kotlin {
    jvm()
    js {
        nodejs()
        useCommonJs()
    }
}
dependencies {
    commonMainApi(project(":libraries:model"))
    commonMainApi("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    commonTestImplementation(project(":libraries:test-logging"))
    commonTestImplementation("com.zegreatrob.testmints:standard")
    commonTestImplementation("com.zegreatrob.testmints:minassert")
    commonTestImplementation("org.jetbrains.kotlin:kotlin-test")

    "jvmMainApi"(kotlin("reflect"))
    "jvmMainApi"("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    "jvmTestImplementation"(kotlin("reflect"))
    "jvmTestImplementation"("org.junit.jupiter:junit-jupiter-api")
    "jvmTestImplementation"("org.junit.jupiter:junit-jupiter-engine")
}
