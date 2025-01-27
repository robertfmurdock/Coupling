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
    commonMainApi(project(":libraries:repository:core"))
    commonMainApi(project(":libraries:test-logging"))
    commonMainApi(project(":libraries:stub-model"))
    commonMainApi("org.jetbrains.kotlinx:kotlinx-datetime")
    commonMainApi("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    commonMainApi("org.jetbrains.kotlin:kotlin-test")
    commonMainApi("com.zegreatrob.testmints:standard")
    commonMainApi("com.zegreatrob.testmints:async")
    commonMainApi("com.zegreatrob.testmints:minassert")

    "jvmMainApi"(kotlin("reflect"))
    "jvmMainImplementation"(kotlin("reflect"))
    "jvmMainImplementation"("org.jetbrains.kotlin:kotlin-test-junit")
}
