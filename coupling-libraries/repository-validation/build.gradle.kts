plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}
group = "com.zegreatrob.coupling.libraries"
kotlin {
    targets {
        jvm()
        js {
            nodejs()
            useCommonJs()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":coupling-libraries:repository-core"))
                api(project(":coupling-libraries:test-logging"))
                api(project(":coupling-libraries:stub-model"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                api("org.jetbrains.kotlin:kotlin-test")
                api("com.zegreatrob.testmints:standard")
                api("com.zegreatrob.testmints:async")
                api("com.zegreatrob.testmints:minassert")
            }
        }

        val jvmMain by getting {
            dependencies {
                api(kotlin("reflect"))
                implementation(kotlin("reflect"))
                implementation("org.jetbrains.kotlin:kotlin-test-junit")
            }
        }

        val jsMain by getting {
            dependencies {
                api("org.jetbrains.kotlin:kotlin-stdlib-js")
            }
        }
    }
}
