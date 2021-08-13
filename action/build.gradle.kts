import com.zegreatrob.coupling.build.BuildConstants

plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization") version "1.5.21"
}

kotlin {

    targets {   
        js {
            nodejs {
                testTask {
                    useMocha {
                        timeout = "10s"
                    }
                }
            }
            useCommonJs()
        }
        jvm()
    }

    sourceSets {
        all {
            languageSettings {
                useExperimentalAnnotation("kotlinx.serialization.ExperimentalSerializationApi")
            }
        }

        getByName("commonMain") {
            dependencies {
                implementation(project(":model"))
                implementation(project(":logging"))
                implementation("com.zegreatrob.testmints:action:4.1.11")
                implementation("com.zegreatrob.testmints:action-async:4.1.11")
                implementation("com.benasher44:uuid:0.3.0")
                implementation("com.soywiz.korlibs.klock:klock:2.1.0")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
                implementation("io.github.microutils:kotlin-logging:2.0.10")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.2.2")
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(project(":json"))
                implementation(project(":test-action"))
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("com.zegreatrob.testmints:standard:4.1.2")
                implementation("com.zegreatrob.testmints:minassert:4.1.2")
                implementation(project(":test-logging"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation("io.github.microutils:kotlin-logging:2.0.10")
                implementation("com.fasterxml.jackson.core:jackson-databind:2.13.0-rc1")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation("org.slf4j:slf4j-simple:2.0.0-alpha4")
                implementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.7.2")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.2.2")
            }
        }
        val jsTest by getting {
            dependencies {
            }
        }
    }
}

tasks {

    val jvmTest by getting(Test::class) {
        systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")

        useJUnitPlatform()
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}