plugins {
    base
}

tasks {
    val check by getting {
        dependsOn(project.getTasksByName("check", true).filterNot { it.project == this.project })
    }
    val collectResults by creating {
        dependsOn(project.getTasksByName("collectResults", true).filterNot { it.project == this.project })
    }
}