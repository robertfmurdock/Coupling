plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    kotlin("plugin.serialization")
}

kotlin {

    targets {
        js {
            nodejs { testTask { useMocha { timeout = "10s" } } }
        }
        jvm()
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(project(":coupling-libraries:model"))
                implementation(project(":coupling-libraries:logging"))
                api("com.zegreatrob.testmints:action")
                api("com.zegreatrob.testmints:action-async")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("com.benasher44:uuid")
                implementation("com.soywiz.korlibs.klock:klock")
                implementation("io.github.microutils:kotlin-logging")
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(project(":coupling-libraries:json"))
                implementation(project(":coupling-libraries:test-logging"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:minassert")
            }
        }
        getByName("jvmMain") {
            dependencies {
                implementation(kotlin("reflect"))
                implementation("io.github.microutils:kotlin-logging")
                implementation("com.fasterxml.jackson.core:jackson-databind")
            }
        }
        getByName("jvmTest") {
            dependencies {
                implementation(kotlin("reflect"))
                implementation("org.slf4j:slf4j-simple")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-engine")
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
