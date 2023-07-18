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
    commonMainImplementation("io.github.microutils:kotlin-logging")
    commonMainImplementation("org.jetbrains.kotlin:kotlin-stdlib-js")
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    commonTestApi(project(":libraries:stub-model"))
    commonTestApi(project(":libraries:test-action"))
    commonTestImplementation(project(":libraries:repository:memory"))
    commonTestImplementation("com.zegreatrob.testmints:async")
    commonTestImplementation("com.zegreatrob.testmints:minassert")
    commonTestImplementation("com.zegreatrob.testmints:minspy")
    commonTestImplementation("com.zegreatrob.testmints:standard")
    commonTestImplementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
    commonTestImplementation("org.jetbrains.kotlin:kotlin-test-common")

    "jsTestApi"(project(":libraries:logging"))
    "jsTestImplementation"("org.jetbrains.kotlin:kotlin-test-js")
}
tasks {
    "formatKotlinJsMain" {
        dependsOn("kspKotlinJs")
    }
    "lintKotlinJsMain" {
        dependsOn("kspKotlinJs")
    }
}