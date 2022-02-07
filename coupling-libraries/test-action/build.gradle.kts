import com.zegreatrob.coupling.build.BuildConstants

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
                implementation("com.zegreatrob.coupling.libraries:action")
                implementation(project(":test-logging"))
                implementation("com.zegreatrob.testmints:action")
                implementation("com.zegreatrob.testmints:action-async")
                implementation("com.zegreatrob.testmints:async")
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:minassert")
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation("org.slf4j:slf4j-simple:2.0.0-alpha6")

                implementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")

                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation("io.github.microutils:kotlin-logging:2.1.21")
                implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
            }
        }
    }
}

tasks {
    val jvmTest by getting(Test::class) {
        systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")

        useJUnitPlatform()
    }
}