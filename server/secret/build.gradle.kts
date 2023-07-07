plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
}
kotlin {
    js {
        moduleName = "Coupling-server-secret"
        nodejs()
    }
}


dependencies {
    implementation(project(":libraries:model"))
    implementation(project(":server:action"))
    implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-node")
    testImplementation(project(":libraries:stub-model"))
    testImplementation(kotlin("test"))
    testImplementation("com.zegreatrob.testmints:async")
    testImplementation("com.zegreatrob.testmints:standard")
    testImplementation("com.zegreatrob.testmints:minassert")
}
