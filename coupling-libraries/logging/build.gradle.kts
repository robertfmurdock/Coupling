repositories {
    mavenCentral()
}

plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    id("com.zegreatrob.coupling.plugins.serialization")
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
                implementation("com.soywiz.korlibs.klock:klock:3.0.0")
                api("io.github.microutils:kotlin-logging:2.1.23")
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
