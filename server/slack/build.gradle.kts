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
    jsMainImplementation(npmConstrained("@slack/webhook"))
    jsMainImplementation(npmConstrained("@slack/oauth"))
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-node")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation("com.zegreatrob.testmints:standard")
    jsTestImplementation("com.zegreatrob.testmints:minassert")
    jsTestImplementation("org.jetbrains.kotlin:kotlin-test")
}
