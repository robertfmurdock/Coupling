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

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                implementation("com.soywiz.korlibs.klock:klock")
                api("io.github.microutils:kotlin-logging")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json")
            }
        }
        getByName("jsMain") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
            }
        }
    }
}
