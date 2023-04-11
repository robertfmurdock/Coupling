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
}

dependencies {
    commonMainApi(enforcedPlatform(project(":coupling-libraries:dependency-bom")))
    commonMainApi(kotlin("stdlib"))
    commonMainApi(kotlin("stdlib-common"))
    commonMainApi("com.soywiz.korlibs.klock:klock")
    commonMainApi("org.jetbrains.kotlinx:kotlinx-datetime")
    commonMainApi("com.benasher44:uuid")
    commonTestImplementation(project(":coupling-libraries:test-logging"))
    commonTestImplementation(kotlin("test"))
    commonTestImplementation("com.zegreatrob.testmints:standard")
    commonTestImplementation("com.zegreatrob.testmints:minassert")
    "jvmTestImplementation"("org.junit.jupiter:junit-jupiter-api")
    "jvmTestImplementation"("org.junit.jupiter:junit-jupiter-engine")
    "jsMainApi"(kotlin("stdlib-js"))
    "jsTestImplementation"(kotlin("test-js"))
}
