plugins {
    id("com.zegreatrob.coupling.plugins.deploy")
}

tasks {
    named("prune") {
        mustRunAfter(":server:serverlessBuildSandbox")
    }
    named("deploy") {
        dependsOn(":server:serverlessBuildSandbox")
    }
}
