plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
}

kotlin {
    js {
        moduleName = "Coupling-client-action"
        nodejs()
        compilations.named("test") {
            packageJson {
                customField("mocha", mapOf("require" to "global-jsdom/register"))
            }
        }
    }
}

dependencies {
    implementation(project(":libraries:action"))
    implementation(project(":libraries:json"))
    implementation(project(":libraries:model"))
    implementation(project(":libraries:repository:core"))
    implementation("com.zegreatrob.testmints:action")
    implementation("com.zegreatrob.testmints:action-async")
    implementation("com.zegreatrob.testmints:minspy")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")

    testImplementation(project(":libraries:stub-model"))
    testImplementation(project(":libraries:test-logging"))
    testImplementation("com.zegreatrob.testmints:async")
}
