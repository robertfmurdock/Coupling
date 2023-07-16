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
    commonMainImplementation("org.jetbrains.kotlin:kotlin-stdlib-common")
    commonMainImplementation("io.github.microutils:kotlin-logging")
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-datetime")
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    "jsMainImplementation"("org.jetbrains.kotlin:kotlin-stdlib-js")
}
