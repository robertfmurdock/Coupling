plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    id("com.zegreatrob.coupling.plugins.serialization")
}
kotlin {
    targets {
        jvm()
        js {
            useCommonJs()
            nodejs()
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":coupling-libraries:model"))
                implementation(kotlin("stdlib"))
                implementation(kotlin("stdlib-common"))
                implementation("com.soywiz.korlibs.klock:klock:3.0.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
                implementation("io.ktor:ktor-client-core")
                implementation("io.ktor:ktor-serialization-kotlinx-json")
                implementation("io.ktor:ktor-client-content-negotiation")
                implementation("io.ktor:ktor-client-logging")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":coupling-libraries:test-logging"))
                implementation(project(":coupling-libraries:stub-model"))
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:minassert")
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("reflect"))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("reflect"))
                implementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.9.0")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }
    }
}

tasks {
}
