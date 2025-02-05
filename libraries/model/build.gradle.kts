plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}
kotlin {
    jvm()
    js {
        nodejs()
        compilerOptions {
            target = "es2015"
            freeCompilerArgs.add("-Xir-per-module")
        }
        compilations.named("test") {
            compileTaskProvider {
                compilerOptions {
                    target = "es5"
                }
            }
        }
    }
}

dependencies {
    commonMainApi(enforcedPlatform(project(":libraries:dependency-bom")))
    commonMainApi("com.benasher44:uuid")
    commonMainApi("org.jetbrains.kotlinx:kotlinx-datetime")
    commonMainApi("org.kotools:types")
    commonTestImplementation(project(":libraries:test-logging"))
    commonTestImplementation(project(":libraries:stub-model"))
    commonTestImplementation(kotlin("test"))
    commonTestImplementation("com.zegreatrob.testmints:standard")
    commonTestImplementation("com.zegreatrob.testmints:minassert")
    "jvmTestImplementation"("org.junit.jupiter:junit-jupiter-api")
    "jvmTestImplementation"("org.junit.jupiter:junit-jupiter-engine")
    "jsTestImplementation"(kotlin("test-js"))
}
