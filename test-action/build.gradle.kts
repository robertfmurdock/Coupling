import com.zegreatrob.coupling.build.BuildConstants

plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization") version "1.5.0"
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
                implementation(project(":action"))
                implementation(project(":test-logging"))
                implementation("com.zegreatrob.testmints:action:4.0.13")
                implementation("com.zegreatrob.testmints:action-async:4.0.13")
                implementation("com.zegreatrob.testmints:async:4.0.13")
                implementation("com.zegreatrob.testmints:standard:4.0.13")
                implementation("com.zegreatrob.testmints:minassert:4.0.13")
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.2.1")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation("org.slf4j:slf4j-simple:2.0.0-alpha1")

                implementation("org.junit.jupiter:junit-jupiter-api:5.8.0-M1")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.8.0-M1")

                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation("io.github.microutils:kotlin-logging:2.0.6")
                implementation("com.fasterxml.jackson.core:jackson-databind:2.12.3")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.2.1")
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