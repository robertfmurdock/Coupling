plugins {
    kotlin("plugin.serialization")
    id("com.zegreatrob.coupling.plugins.mp")
}
kotlin {
    targets {
        jvm()
        js { nodejs() }
    }
}
dependencies {
    commonMainApi("io.github.oshai:kotlin-logging")
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-datetime")
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
}
