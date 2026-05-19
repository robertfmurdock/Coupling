package com.zegreatrob.coupling.plugins

import org.gradle.api.tasks.testing.TestDescriptor
import java.util.UUID

internal class TestIdentityTracker(
    private val taskName: String,
    private val testRunIdentifier: String,
) {
    private val testIdentityByDescriptor = mutableMapOf<Int, TestIdentity>()
    private val testOccurrenceByIdentityKey = mutableMapOf<String, Int>()

    data class TestIdentity(
        val suite: String,
        val test: String,
        val testId: String,
    )

    fun identityForStart(testDescriptor: TestDescriptor): TestIdentity = synchronized(this) {
        computeIdentity(testDescriptor, assignOccurrence = true).also {
            testIdentityByDescriptor[descriptorKey(testDescriptor)] = it
        }
    }

    fun identityForEnd(testDescriptor: TestDescriptor): TestIdentity = synchronized(this) {
        testIdentityByDescriptor[descriptorKey(testDescriptor)]
            ?: computeIdentity(testDescriptor, assignOccurrence = true).also {
                testIdentityByDescriptor[descriptorKey(testDescriptor)] = it
            }
    }

    fun identityForLog(testDescriptor: TestDescriptor?): TestIdentity? = synchronized(this) {
        testDescriptor ?: return null
        testIdentityByDescriptor[descriptorKey(testDescriptor)]
            ?: computeIdentity(testDescriptor, assignOccurrence = false)
    }

    fun clearIdentity(testDescriptor: TestDescriptor) = synchronized(this) {
        testIdentityByDescriptor.remove(descriptorKey(testDescriptor))
    }

    private fun descriptorKey(testDescriptor: TestDescriptor): Int = System.identityHashCode(testDescriptor)

    private fun computeIdentity(testDescriptor: TestDescriptor, assignOccurrence: Boolean): TestIdentity {
        val suite = suiteName(testDescriptor)
        val test = testName(testDescriptor)
        val identityKey = "$taskName||$suite||$test"
        val occurrence = if (assignOccurrence) {
            val next = (testOccurrenceByIdentityKey[identityKey] ?: 0) + 1
            testOccurrenceByIdentityKey[identityKey] = next
            next
        } else {
            testOccurrenceByIdentityKey[identityKey] ?: 1
        }
        val opaque = UUID.nameUUIDFromBytes(
            "$testRunIdentifier||$taskName||$suite||$test||$occurrence".toByteArray(),
        ).toString()
        return TestIdentity(
            suite = suite,
            test = test,
            testId = opaque,
        )
    }

    private fun suiteName(testDescriptor: TestDescriptor): String = testDescriptor.className?.takeIf { it.isNotBlank() }
        ?: testDescriptor.parent?.name?.takeIf { it.isNotBlank() }
        ?: "unknown-suite"

    private fun testName(testDescriptor: TestDescriptor): String = testDescriptor.name.takeIf { it.isNotBlank() }
        ?: "unknown-test"
}
