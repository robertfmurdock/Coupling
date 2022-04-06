plugins {
    base
    id("com.avast.gradle.docker-compose") version "0.15.2"
}

tasks {
    check {
        dependsOn(project.getTasksByName("check", true).filterNot { it.project == this.project })
    }
    val collectResults by registering {
        dependsOn(project.getTasksByName("collectResults", true).filterNot { it.project == this.project })
    }
}

dockerCompose {
    tcpPortsToIgnoreWhenWaiting.set(listOf(5555))
    projectName = "Coupling-root"
    startedServices.set(listOf("dynamo"))
    containerLogToDir.set(project.file("build/test-output/containers-logs"))
}
