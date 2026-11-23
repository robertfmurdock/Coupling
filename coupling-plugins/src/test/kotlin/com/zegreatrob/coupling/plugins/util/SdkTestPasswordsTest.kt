package com.zegreatrob.coupling.plugins.util

import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.gradle.api.tasks.testing.Test as JvmTest

class SdkTestPasswordsTest {
    @Test
    fun `JVM tasks resolve each account only at execution and replace inherited passwords`() {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("sdkTest", JvmTest::class.java).get()
        val resolved = arrangePasswords(task)

        task.actions.first().execute(task)

        assertInjection(resolved, task.environment.mapValues { it.value.toString() })
    }

    @Test
    fun `Node tasks resolve each account only at execution and replace inherited passwords`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        project.extensions.getByType(KotlinMultiplatformExtension::class.java).js { nodejs() }
        val task = project.tasks.getByName("jsNodeTest") as KotlinJsTest
        val resolved = arrangePasswords(task)

        task.actions.first().execute(task)

        assertInjection(resolved, task.environment)
    }

    @Test
    fun `lookup failure stops task execution without exposing an inherited password`() {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("sdkTest", JvmTest::class.java).get()
        task.environment("COUPLING_PRIMARY_TEST_PASSWORD", "private-inherited-value")
        task.useSdkTestPasswords { name ->
            project.providers.provider { error("AWS SSM parameter is unavailable: $name.") }
        }

        val failure = assertFailsWith<IllegalStateException> { task.actions.first().execute(task) }

        assertEquals("AWS SSM parameter is unavailable: /local/COUPLING_PRIMARY_TEST_PASSWORD.", failure.message)
    }

    private val variables = listOf("COUPLING_PRIMARY_TEST_PASSWORD", "COUPLING_ALT_TEST_PASSWORD", "COUPLING_E2E_TEST_PASSWORD")

    private fun arrangePasswords(task: Task): List<String> {
        val resolved = mutableListOf<String>()
        variables.forEach { variable ->
            when (task) {
                is JvmTest -> task.environment(variable, "incorrect-inherited-value")
                is KotlinJsTest -> task.environment(variable, "incorrect-inherited-value")
            }
        }
        task.useSdkTestPasswords { name ->
            task.project.providers.provider {
                resolved.add(name)
                "controlled-value-for-$name"
            }
        }
        assertEquals(emptyList(), resolved)

        return resolved
    }

    private fun assertInjection(resolved: List<String>, environment: Map<String, String>) {
        assertEquals(variables.map { "/local/$it" }, resolved)
        assertEquals(
            variables.associateWith { "controlled-value-for-/local/$it" },
            environment.filterKeys { it in variables },
        )
    }
}
