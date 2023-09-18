plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}

kotlin {
    jvm()
}

dependencies {
    "jvmTestImplementation"(kotlin("test"))
    "jvmTestImplementation"("com.lemonappdev:konsist")
    "jvmTestImplementation"("com.zegreatrob.testmints:minassert")
    "jvmTestImplementation"("com.zegreatrob.testmints:standard")
}
