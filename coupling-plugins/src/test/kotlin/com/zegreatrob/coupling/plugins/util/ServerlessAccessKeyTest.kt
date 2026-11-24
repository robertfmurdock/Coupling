package com.zegreatrob.coupling.plugins.util

import com.zegreatrob.coupling.plugins.js.NodeExec
import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ServerlessAccessKeyTest {
    @Test
    fun `Serverless task resolves SSM only during execution and replaces an inherited key`() {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("serverlessPackage", NodeExec::class.java).get()
        val resolved = mutableListOf<String>()
        task.environment("SERVERLESS_ACCESS_KEY", "incorrect-inherited-key")
        task.useServerlessAccessKey { name ->
            project.providers.provider {
                resolved.add(name)
                "controlled-ssm-key"
            }
        }
        assertEquals(emptyList(), resolved)

        task.actions.first().execute(task)

        assertEquals(listOf("/local/SERVERLESS_ACCESS_KEY"), resolved)
        assertEquals("controlled-ssm-key", task.environment["SERVERLESS_ACCESS_KEY"])
    }

    @Test
    fun `missing SSM key fails before the Serverless process can use an inherited key`() {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("serverlessPackage", NodeExec::class.java).get()
        task.environment("SERVERLESS_ACCESS_KEY", "private-inherited-key")
        task.useServerlessAccessKey { name ->
            project.providers.provider { error("AWS SSM parameter is unavailable: $name.") }
        }

        val failure = assertFailsWith<IllegalStateException> { task.actions.first().execute(task) }

        assertEquals("AWS SSM parameter is unavailable: /local/SERVERLESS_ACCESS_KEY.", failure.message)
    }
}
