plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
}
kotlin {
    js {
        outputModuleName = "Coupling-server-secret"
        nodejs()
        useEsModules()
        compilerOptions { target = "es2015" }
    }
}


dependencies {
    jsMainImplementation(project(":libraries:model"))
    jsMainImplementation(project(":server:actionz"))
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-node")
    jsTestImplementation(project(":libraries:stub-model"))
    jsTestImplementation(kotlin("test"))
    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation("com.zegreatrob.testmints:standard")
    jsTestImplementation("com.zegreatrob.testmints:minassert")
}
