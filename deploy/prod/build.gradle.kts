plugins {
    id("com.zegreatrob.coupling.plugins.deploy")
}

tasks {
    named("prune") {
        mustRunAfter(":server:serverlessBuild")
    }
    named("deploy") {
        dependsOn(":server:serverlessBuild")
    }
}
