plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    targets {
        jvm()
        js { nodejs() }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":model"))
                api(kotlin("stdlib", com.zegreatrob.coupling.build.BuildConstants.kotlinVersion))
                api(kotlin("stdlib-common", com.zegreatrob.coupling.build.BuildConstants.kotlinVersion))
                api("com.soywiz.korlibs.klock:klock:1.8.9")
                api("com.benasher44:uuid:0.0.7")
            }
        }
        val jsMain by getting {
            dependencies {
                api(project(":json"))
                api(project(":mongo"))

                api(kotlin("stdlib-js", com.zegreatrob.coupling.build.BuildConstants.kotlinVersion))
            }
        }
    }
}
