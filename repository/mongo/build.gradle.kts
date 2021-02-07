
import com.zegreatrob.coupling.build.BuildConstants

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

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
                api(project(":model"))
                api(project(":repository"))
                implementation("com.benasher44:uuid:0.2.3")
                implementation("com.soywiz.korlibs.klock:klock:1.12.0")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
            }
        }
        getByName("commonTest") {
            dependencies {
                api(project(":repository:validation"))
                api(project(":stub-model"))
                implementation("com.zegreatrob.testmints:standard:3.2.1")
                implementation("com.zegreatrob.testmints:minassert:3.2.1")
                implementation("com.zegreatrob.testmints:async:3.2.1")
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
            }
        }

        val jsMain by getting {
            dependencies {
            }
        }
        val jsTest by getting {
            dependencies {
                api(project(":logging"))
                implementation(npm("monk", "7.1.1"))
                implementation(npm("mongodb", "3.5.0"))
            }
        }
    }
}

tasks {
}
