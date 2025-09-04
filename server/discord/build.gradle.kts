plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    kotlin("plugin.serialization")
}
kotlin {
    js {
        nodejs()
        useEsModules()
        compilerOptions { target = "es2015" }
    }
}


dependencies {
    jsMainImplementation("io.ktor:ktor-client-content-negotiation")
    jsMainImplementation("io.ktor:ktor-client-core")
    jsMainImplementation("io.ktor:ktor-client-logging")
    jsMainImplementation("io.ktor:ktor-serialization-kotlinx-json")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-node")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation("com.zegreatrob.testmints:minassert")
    jsTestImplementation("com.zegreatrob.testmints:standard")
    jsTestImplementation("io.ktor:ktor-client-mock")
    jsTestImplementation("org.jetbrains.kotlin:kotlin-test")
}
