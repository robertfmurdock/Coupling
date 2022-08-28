plugins {
    id("com.zegreatrob.coupling.plugins.mp")
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
        getByName("commonMain") {
            dependencies {
                implementation(project(":coupling-libraries:model"))
                implementation(project(":coupling-libraries:repository-core"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("com.benasher44:uuid:0.5.0")
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(project(":coupling-libraries:test-logging"))
                implementation(project(":coupling-libraries:repository-validation"))
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:minassert")
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }
        getByName("jvmMain") {
            dependencies {
                api(kotlin("reflect"))
            }
        }
        getByName("jvmTest") {
            dependencies {
                implementation(kotlin("reflect"))
                implementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.9.0")
            }
        }

        getByName("jsMain") {
            dependencies {
                api("org.jetbrains.kotlin:kotlin-stdlib-js")
            }
        }
        getByName("jsTest") {
            dependencies {
                implementation("com.zegreatrob.testmints:async")
            }
        }
    }
}
