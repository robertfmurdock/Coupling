plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}
kotlin {
    targets {
        jvm()
        js { nodejs() }
    }
}

dependencies {
    commonMainApi(project(":coupling-libraries:model"))
    commonMainApi(project(":coupling-libraries:repository-core"))
    commonMainApi("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    commonTestImplementation(project(":coupling-libraries:test-logging"))
    commonTestImplementation(project(":coupling-libraries:repository-memory"))
    commonTestImplementation(project(":coupling-libraries:repository-validation"))
    commonTestImplementation("com.zegreatrob.testmints:standard")
    commonTestImplementation("com.zegreatrob.testmints:minassert")
    commonTestImplementation("org.jetbrains.kotlin:kotlin-test")
    "jvmMainApi"(kotlin("reflect"))
    "jvmTestImplementation"(kotlin("reflect"))
    "jvmTestImplementation"("org.junit.jupiter:junit-jupiter-api")
    "jvmTestImplementation"("org.junit.jupiter:junit-jupiter-engine")
    "jsTestImplementation"("com.zegreatrob.testmints:async")
    "jsMainApi"("org.jetbrains.kotlin:kotlin-stdlib-js")
}
