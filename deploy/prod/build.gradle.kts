plugins {
    id("com.zegreatrob.coupling.plugins.deploy")
}

tasks {
    "deploy" {
        dependsOn(":server:serverlessBuild")
    }
}
