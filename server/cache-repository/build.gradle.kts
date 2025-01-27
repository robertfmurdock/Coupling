plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}
kotlin {
    js {
        nodejs()
    }
}
dependencies {
    "jsMainApi"(project(":libraries:repository:core"))
    "jsMainApi"(project(":libraries:model"))
    "jsMainApi"("com.benasher44:uuid")
    "jsMainApi"("io.github.oshai:kotlin-logging")
    "jsMainApi"("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    "jsTestImplementation"(project(":libraries:stub-model"))
    "jsTestImplementation"(project(":libraries:repository:memory"))
    "jsTestImplementation"(project(":libraries:logging"))
    "jsTestImplementation"("com.zegreatrob.testmints:async")
    "jsTestImplementation"("com.zegreatrob.testmints:minassert")
    "jsTestImplementation"("com.zegreatrob.testmints:minspy")
    "jsTestImplementation"("com.zegreatrob.testmints:standard")
    "jsTestImplementation"("org.jetbrains.kotlin:kotlin-test")
    "jsTestImplementation"("org.jetbrains.kotlin:kotlin-test-annotations-common")
}
