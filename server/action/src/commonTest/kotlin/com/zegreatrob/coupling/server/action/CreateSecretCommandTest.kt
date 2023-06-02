package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.memory.MemorySecretRepository
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class CreateSecretCommandTest {
    @Test
    fun willSaveAndGenerateSecret() = asyncSetup(object : CreateSecretCommand.Dispatcher {
        override val secretRepository = MemorySecretRepository()
        val expectedSecretToken = uuidString()
        val spy = SpyData<PartyElement<Secret>, String>().apply {
            spyWillReturn(expectedSecretToken)
        }
        override val secretGenerator = SecretGenerator(spy::spyFunction)
        val partyId = stubPartyId()
    }) exercise {
        perform(CreateSecretCommand(partyId))
    } verify { (secret, token) ->
        token.assertIsEqualTo(expectedSecretToken)
        secretRepository.getSecrets(partyId).elements
            .assertIsEqualTo(listOf(secret))
        spy.spyReceivedValues
            .assertIsEqualTo(listOf(partyId.with(secret)))
    }
}
