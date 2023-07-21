plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    id("com.zegreatrob.testmints.action-mint")
}
kotlin {
    targets {
        js {
            moduleName = "Coupling-server-action"
            nodejs()
            useCommonJs()
        }
    }
    sourceSets.named("jsMain") {
        kotlin.srcDir("build/generated/ksp/js/jsMain/kotlin")
        kotlin.srcDir("src/commonMain/kotlin")
    }
}
dependencies {
    commonMainApi(project(":libraries:repository:core"))
    commonMainApi(project(":libraries:model"))
    commonMainApi(project(":libraries:action"))
    commonMainApi("com.zegreatrob.testmints:action")
    commonMainApi("com.zegreatrob.testmints:action-async")
    commonMainImplementation("com.benasher44:uuid")
    commonMainImplementation("io.github.oshai:kotlin-logging")
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    commonTestImplementation(project(":libraries:stub-model"))
    commonTestImplementation(project(":libraries:test-action"))
    commonTestImplementation(project(":libraries:repository:memory"))
    commonTestImplementation("com.zegreatrob.testmints:async")
    commonTestImplementation("com.zegreatrob.testmints:minassert")
    commonTestImplementation("com.zegreatrob.testmints:minspy")
    commonTestImplementation("com.zegreatrob.testmints:standard")
    commonTestImplementation("org.jetbrains.kotlin:kotlin-test")
    commonTestImplementation("org.jetbrains.kotlin:kotlin-test-annotations-common")

    "jsTestImplementation"(project(":libraries:logging"))
}
tasks {
    "formatKotlinJsMain" {
        dependsOn("kspKotlinJs")
    }
    "lintKotlinJsMain" {
        dependsOn("kspKotlinJs")
    }
}
