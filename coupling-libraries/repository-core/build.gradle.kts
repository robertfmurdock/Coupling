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
                api(project(":coupling-libraries:model"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(project(":coupling-libraries:test-logging"))
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:minassert")
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }
        getByName("jvmMain") {
            dependencies {
                api(kotlin("reflect"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
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
    }
}
