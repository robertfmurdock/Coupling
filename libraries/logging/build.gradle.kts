plugins {
    kotlin("plugin.serialization")
    id("com.zegreatrob.coupling.plugins.mp")
}
kotlin {
    jvm()
    js {
        nodejs()
        compilerOptions { target = "es2015" }
    }
}

dependencies {
    commonMainApi("io.github.oshai:kotlin-logging")
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-datetime")
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
}
