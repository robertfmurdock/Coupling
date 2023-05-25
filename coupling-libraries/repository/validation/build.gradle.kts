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
                api(project(":coupling-libraries:repository:core"))
                api(project(":coupling-libraries:test-logging"))
                api(project(":coupling-libraries:stub-model"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                api("org.jetbrains.kotlin:kotlin-test")
                api("com.zegreatrob.testmints:standard")
                api("com.zegreatrob.testmints:async")
                api("com.zegreatrob.testmints:minassert")
            }
        }

        getByName("jvmMain") {
            dependencies {
                api(kotlin("reflect"))
                implementation(kotlin("reflect"))
                implementation("org.jetbrains.kotlin:kotlin-test-junit")
            }
        }
        getByName("jsMain") {
            dependencies {
                api("org.jetbrains.kotlin:kotlin-stdlib-js")
            }
        }
    }
}
