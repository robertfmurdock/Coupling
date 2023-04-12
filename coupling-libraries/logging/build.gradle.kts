repositories {
    mavenCentral()
}

plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    kotlin("plugin.serialization")
}

kotlin {
    targets {
        jvm()
        js {
            nodejs()
            useCommonJs()
        }
    }
}

dependencies {
    commonMainImplementation("org.jetbrains.kotlin:kotlin-stdlib-common")
    commonMainImplementation("com.soywiz.korlibs.klock:klock")
    commonMainApi("io.github.microutils:kotlin-logging")
    commonMainApi("org.jetbrains.kotlinx:kotlinx-serialization-json")
    "jsMainImplementation"("org.jetbrains.kotlin:kotlin-stdlib-js")
}
