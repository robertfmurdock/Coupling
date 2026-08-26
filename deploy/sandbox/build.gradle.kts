plugins {
    id("com.zegreatrob.coupling.plugins.deploy")
}

tasks {
    named("prune") {
        mustRunAfter("serverlessPackage")
    }
    named("deploy") {
        dependsOn("serverlessPackage")
    }
}
