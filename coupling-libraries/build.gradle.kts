plugins {
    base
}

tasks {
    check {
        dependsOn(project.getTasksByName("check", true).filterNot { it.project == this.project })
    }
    val collectResults by registering {
        dependsOn(project.getTasksByName("collectResults", true).filterNot { it.project == this.project })
    }
}
