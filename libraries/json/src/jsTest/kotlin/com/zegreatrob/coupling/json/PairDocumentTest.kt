package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.json.encodeToDynamic
import kotlin.test.Test
import kotlin.time.Instant

class PairDocumentTest {

    @Test
    fun canHandleMilliseconds() = setup(object {
        val pairs = stubPairAssignmentDoc()
            .copy(date = Instant.fromEpochMilliseconds(1644075138096))
    }) exercise {
        pairs.toSerializable()
            .toJsonElement()
            .fromJsonElement<JsonPairingSet>()
            .toModel()
    } verify { result ->
        result.assertIsEqualTo(pairs)
    }

    @Test
    fun roundTrip() = setup(object {
        val contributionsInput =
            GqlContributionsInput(window = GqlContributionWindow.Week, limit = null)
    }) exercise {
        contributionsInput
            .let { couplingJsonFormat.encodeToDynamic(it) }
    } verify { result ->
        couplingJsonFormat.decodeFromDynamic<GqlContributionsInput>(result)
            .assertIsEqualTo(contributionsInput)
    }
}
