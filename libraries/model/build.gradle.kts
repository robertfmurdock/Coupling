plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}
kotlin {
    jvm()
    js {
        nodejs()
        useCommonJs()
        compilerOptions { target = "es2015" }
    }
}
dependencies {
    commonMainApi(enforcedPlatform(project(":libraries:dependency-bom")))
    commonMainApi("com.benasher44:uuid")
    commonMainApi("org.jetbrains.kotlinx:kotlinx-datetime")
    commonMainApi("org.kotools:types")
    commonTestImplementation(project(":libraries:test-logging"))
    commonTestImplementation(project(":libraries:stub-model"))
    commonTestImplementation(kotlin("test"))
    commonTestImplementation("com.zegreatrob.testmints:standard")
    commonTestImplementation("com.zegreatrob.testmints:minassert")
    "jvmTestImplementation"("org.junit.jupiter:junit-jupiter-api")
    "jvmTestImplementation"("org.junit.jupiter:junit-jupiter-engine")
    "jsTestImplementation"(kotlin("test-js"))
}
