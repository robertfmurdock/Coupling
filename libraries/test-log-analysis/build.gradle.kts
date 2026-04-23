plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}

kotlin {
    jvm()
}

dependencies {
    "jvmMainImplementation"("com.fasterxml.jackson.core:jackson-databind")
    "jvmTestImplementation"("com.zegreatrob.testmints:minassert")
    "jvmTestImplementation"("com.zegreatrob.testmints:standard")
    "jvmTestImplementation"(kotlin("test"))
}
