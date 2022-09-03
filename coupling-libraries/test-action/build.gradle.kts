plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    kotlin("plugin.serialization")
}
kotlin {

    targets {
        js {
            nodejs()
            useCommonJs()
        }
        jvm()
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(project(":coupling-libraries:action"))
                implementation(project(":coupling-libraries:test-logging"))
                api("com.zegreatrob.testmints:action")
                api("com.zegreatrob.testmints:action-async")
                api("com.zegreatrob.testmints:async")
                api("com.zegreatrob.testmints:standard")
                api("com.zegreatrob.testmints:minassert")
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
            }
        }
        getByName("jvmMain") {
            dependencies {
                implementation(kotlin("reflect"))
                implementation("org.slf4j:slf4j-simple")

                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-engine")

                implementation(kotlin("reflect"))
                implementation("io.github.microutils:kotlin-logging")
                implementation("com.fasterxml.jackson.core:jackson-databind")
            }
        }
        getByName("jsMain") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
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
