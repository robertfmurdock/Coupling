import com.zegreatrob.coupling.build.BuildConstants

plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}
group = "com.zegreatrob.coupling.libraries"
kotlin {
    targets {
        js {
            nodejs()
            useCommonJs()
        }
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                api("com.zegreatrob.coupling.libraries:repository-core")
                api("com.zegreatrob.coupling.libraries:model")
                api("com.zegreatrob.coupling.libraries:action")
                api("com.zegreatrob.testmints:action")
                api("com.zegreatrob.testmints:action-async")
                implementation("com.benasher44:uuid:0.4.0")
                implementation("com.soywiz.korlibs.klock:klock:2.4.13")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("io.github.microutils:kotlin-logging:2.1.21")
            }
        }
        getByName("commonTest") {
            dependencies {
                api("com.zegreatrob.coupling.libraries:stub-model")
                api("com.zegreatrob.coupling.libraries:test-action")
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:async")
                implementation("com.zegreatrob.testmints:minassert")
                implementation("com.zegreatrob.testmints:minspy")
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
            }
        }

        val jsTest by getting {
            dependencies {
                api("com.zegreatrob.coupling.libraries:logging")
            }
        }
    }
}
