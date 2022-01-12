
import com.zegreatrob.coupling.build.BuildConstants

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
        val commonMain by getting {
            dependencies {
                api(project(":model"))
                api(kotlin("stdlib", BuildConstants.kotlinVersion))
                api(kotlin("stdlib-common", BuildConstants.kotlinVersion))
                api("com.soywiz.korlibs.klock:klock:2.4.12")
                api("com.benasher44:uuid:0.3.1")
            }
        }
        val jsMain by getting {
            dependencies {
                api(kotlin("stdlib-js", BuildConstants.kotlinVersion))
            }
        }
    }
}
