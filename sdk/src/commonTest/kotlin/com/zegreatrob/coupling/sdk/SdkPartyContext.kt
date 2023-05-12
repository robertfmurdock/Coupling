package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PartyContext
import com.zegreatrob.coupling.stubmodel.stubUser

class SdkPartyContext<T>(
    private val sdk: Sdk,
    override val repository: T,
    override val partyId: PartyId,
    override val clock: MagicClock,
) : PartyContext<T>, SdkApi by sdk {
    override val user = stubUser().copy(email = primaryAuthorizedUsername)
}
