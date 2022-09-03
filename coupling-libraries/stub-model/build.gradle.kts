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
                api(kotlin("stdlib"))
                api(kotlin("stdlib-common"))
                api("com.soywiz.korlibs.klock:klock:3.0.1")
                api("com.benasher44:uuid:0.5.0")
            }
        }
        getByName("jsMain") {
            dependencies {
                api(kotlin("stdlib-js"))
            }
        }
    }
}
