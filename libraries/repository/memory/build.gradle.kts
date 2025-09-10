plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}
kotlin {
    jvm()
    js {
        nodejs()
        useEsModules()
        compilerOptions { target = "es2015" }
    }
}

dependencies {
    commonMainImplementation(project(":libraries:model"))
    commonMainImplementation(project(":libraries:repository:core"))
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    commonTestImplementation(project(":libraries:test-logging"))
    commonTestImplementation(project(":libraries:repository:validation"))
    commonTestImplementation("com.zegreatrob.testmints:standard")
    commonTestImplementation("com.zegreatrob.testmints:minassert")
    commonTestImplementation("org.jetbrains.kotlin:kotlin-test")

    "jvmMainApi"(kotlin("reflect"))

    "jvmTestImplementation"(kotlin("reflect"))
    "jvmTestImplementation"("org.junit.jupiter:junit-jupiter-api")
    "jvmTestImplementation"("org.junit.jupiter:junit-jupiter-engine")

    "jsTestImplementation"("com.zegreatrob.testmints:async")
}
