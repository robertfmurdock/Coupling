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
    jsMainImplementation(project(":libraries:model"))
    jsMainImplementation(project(":server:action"))
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-node")
    jsTestImplementation(project(":libraries:stub-model"))
    jsTestImplementation(kotlin("test"))
    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation("com.zegreatrob.testmints:standard")
    jsTestImplementation("com.zegreatrob.testmints:minassert")
}
