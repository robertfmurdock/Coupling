plugins {
    id("com.zegreatrob.coupling.plugins.deploy")
}

tasks {
    named("deploy") {
        dependsOn(":server:serverlessBuild")
    }
}
