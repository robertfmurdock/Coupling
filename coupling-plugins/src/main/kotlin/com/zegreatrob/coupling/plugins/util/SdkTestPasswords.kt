package com.zegreatrob.coupling.plugins.util

import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest

fun Task.useSdkTestPasswords(lookup: (String) -> Provider<String> = project.providers::fetchAwsSsmParameter) {
    val passwords = listOf("PRIMARY", "ALT", "E2E").associate { account ->
        val variable = "COUPLING_${account}_TEST_PASSWORD"
        variable to lookup("/local/$variable")
    }
    doFirst {
        val task = this
        passwords.forEach { (variable, password) ->
            when (task) {
                is Test -> task.environment(variable, password.get())
                is KotlinJsTest -> task.environment(variable, password.get())
            }
        }
    }
}
