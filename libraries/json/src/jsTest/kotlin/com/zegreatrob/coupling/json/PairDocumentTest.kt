package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import korlibs.time.DateTime
import kotlin.test.Test

class PairDocumentTest {

    @Test
    fun canHandleMilliseconds() = setup(object {
        val pairs = stubPairAssignmentDoc()
            .copy(date = DateTime(1644075138096))
    }) exercise {
        pairs.toSerializable()
            .toJsonElement()
            .fromJsonElement<JsonPairAssignmentDocument>()
            .toModel()
    } verify { result ->
        result.assertIsEqualTo(pairs)
    }
}
