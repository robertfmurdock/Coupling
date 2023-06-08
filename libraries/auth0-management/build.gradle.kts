plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    kotlin("plugin.serialization")
}

kotlin {
    targets {
        js { nodejs() }
        jvm()
    }
}

dependencies {
    "commonMainImplementation"("io.ktor:ktor-client-content-negotiation")
    "commonMainImplementation"("io.ktor:ktor-client-core")
    "commonMainImplementation"("io.ktor:ktor-client-logging")
    "commonMainImplementation"("io.ktor:ktor-serialization-kotlinx-json")
    "commonMainImplementation"("org.jetbrains.kotlinx:kotlinx-serialization-json")
    "jvmMainImplementation"("io.ktor:ktor-client-java")
    "jvmMainImplementation"("org.slf4j:slf4j-api")
    "jvmMainImplementation"("org.slf4j:slf4j-simple")
}
