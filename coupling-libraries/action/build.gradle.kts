plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    id("com.zegreatrob.coupling.plugins.serialization")
}
group = "com.zegreatrob.coupling.libraries"
kotlin {

    targets {
        js {
            nodejs { testTask { useMocha { timeout = "10s" } } }
            useCommonJs()
        }
        jvm()
    }

    sourceSets {
        all {
            languageSettings {
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
            }
        }

        getByName("commonMain") {
            dependencies {
                implementation(project(":coupling-libraries:model"))
                implementation(project(":coupling-libraries:logging"))
                api("com.zegreatrob.testmints:action")
                api("com.zegreatrob.testmints:action-async")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("com.benasher44:uuid:0.4.0")
                implementation("com.soywiz.korlibs.klock:klock:2.7.0")
                implementation("io.github.microutils:kotlin-logging:2.1.21")
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(project(":coupling-libraries:json"))
                implementation(project(":coupling-libraries:test-action"))
                implementation(project(":coupling-libraries:test-logging"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:minassert")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("reflect"))
                implementation("io.github.microutils:kotlin-logging:2.1.21")
                implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("reflect"))
                implementation("org.slf4j:slf4j-simple:2.0.0-alpha7")
                implementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
            }
        }
    }
}

tasks {
    named("jvmTest", Test::class) {
        systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")

        useJUnitPlatform()
    }
}
