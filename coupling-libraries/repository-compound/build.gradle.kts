plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}
group = "com.zegreatrob.coupling.libraries"
kotlin {
    targets {
        jvm()
        js { nodejs() }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":coupling-libraries:model"))
                api(project(":coupling-libraries:repository-core"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":coupling-libraries:test-logging"))
                implementation(project(":coupling-libraries:repository-memory"))
                implementation(project(":coupling-libraries:repository-validation"))
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:minassert")
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }

        val jvmMain by getting {
            dependencies {
                api(kotlin("reflect"))
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
                api("org.jetbrains.kotlin:kotlin-stdlib-js")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation("com.zegreatrob.testmints:async")
            }
        }
    }
}
