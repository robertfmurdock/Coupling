plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}
kotlin {
    targets {
        jvm()
        js { nodejs() }
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                api(project(":coupling-libraries:model"))
                api(project(":coupling-libraries:repository-core"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(project(":coupling-libraries:test-logging"))
                implementation(project(":coupling-libraries:repository-memory"))
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
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-engine")
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
