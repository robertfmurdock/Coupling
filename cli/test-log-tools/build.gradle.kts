plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}

kotlin {
    jvm()
}

dependencies {
    "jvmMainImplementation"(project(":libraries:test-log-analysis"))
    "jvmMainImplementation"("com.fasterxml.jackson.core:jackson-databind")
}
