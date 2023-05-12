plugins {
    base
}

tasks {
    check {
        dependsOn(project.getTasksByName("check", true).filterNot { it.project == this.project })
    }
    register("collectResults") {
        dependsOn(project.getTasksByName("collectResults", true).filterNot { it.project == this.project })
    }
}
