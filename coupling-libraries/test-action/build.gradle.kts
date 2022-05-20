plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    id("com.zegreatrob.coupling.plugins.serialization")
}
group = "com.zegreatrob.coupling.libraries"
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

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("reflect"))
                implementation("org.slf4j:slf4j-simple:2.0.0-alpha7")

                implementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")

                implementation(kotlin("reflect"))
                implementation("io.github.microutils:kotlin-logging:2.1.21")
                implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
            }
        }

        val jsMain by getting {
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
