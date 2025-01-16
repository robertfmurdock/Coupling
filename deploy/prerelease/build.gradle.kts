plugins {
    id("com.zegreatrob.coupling.plugins.deploy")
}

tasks {
    named("prune") {
        mustRunAfter(":server:serverlessBuildPrerelease")
    }
    named("deploy") {
        dependsOn(":server:serverlessBuildPrerelease")
    }
}
