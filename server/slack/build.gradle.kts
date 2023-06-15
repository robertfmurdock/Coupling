plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
}
kotlin {
    js {
        nodejs()
    }
}


dependencies {
    implementation(npmConstrained("@slack/web-api"))
    implementation(npmConstrained("@slack/webhook"))
    implementation(npmConstrained("@slack/oauth"))
    implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-node")
    testImplementation("com.zegreatrob.testmints:async")
    testImplementation("com.zegreatrob.testmints:standard")
    testImplementation("com.zegreatrob.testmints:minassert")
}
