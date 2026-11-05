package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test
import kotlin.time.Instant

class BackfillRevisionIdTest {
    @Test
    fun revisionsAreStableUlidsOrderedBySourceTimestamp() = setup(object {
        val earlier = Instant.parse("2024-01-01T00:00:00Z")
        val later = Instant.parse("2024-01-02T00:00:00Z")
    }) exercise {
        listOf(backfillRevisionId(earlier, "party-first"), backfillRevisionId(later, "party-second"))
    } verify { ids ->
        ids.assertIsEqualTo(ids.sorted())
        ids.all { Regex("[0-7][0-9A-HJKMNP-TV-Z]{25}").matches(it) }.assertIsEqualTo(true)
        ids.first().assertIsEqualTo(backfillRevisionId(earlier, "party-first"))
        (ids.first() != backfillRevisionId(earlier, "different-party")).assertIsEqualTo(true)
    }

    @Test
    fun deployedTargetNamespacesAreIndependentOfTheLegacyPrefix() = setup { } exercise {
        listOf(targetTableName("coupling-core", "prod"), targetTableName("coupling-core", "sandbox"), targetTableName("coupling-history", "prerelease"))
    } verify { names ->
        names.assertIsEqualTo(listOf("coupling-core", "sandbox-coupling-core", "prerelease-coupling-history"))
        legacyDynamoPrefix().assertIsEqualTo("test-")
        targetTableName("coupling-core").assertIsEqualTo("test-coupling-core")
    }
}
