plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}
kotlin {
    targets {
        js {
            moduleName = "Coupling-server-action"
            nodejs()
            useCommonJs()
        }
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                api(project(":coupling-libraries:repository-core"))
                api(project(":coupling-libraries:model"))
                api(project(":coupling-libraries:action"))
                api("com.zegreatrob.testmints:action")
                api("com.zegreatrob.testmints:action-async")
                implementation("com.benasher44:uuid")
                implementation("com.soywiz.korlibs.klock:klock")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("io.github.microutils:kotlin-logging")
            }
        }
        getByName("commonTest") {
            dependencies {
                api(project(":coupling-libraries:stub-model"))
                api(project(":coupling-libraries:test-action"))
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:async")
                implementation("com.zegreatrob.testmints:minassert")
                implementation("com.zegreatrob.testmints:minspy")
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
            }
        }

        getByName("jsTest") {
            dependencies {
                api(project(":coupling-libraries:logging"))
            }
        }
    }
}
