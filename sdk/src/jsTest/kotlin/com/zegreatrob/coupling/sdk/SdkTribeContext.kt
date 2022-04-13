package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.user.UserQuery
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.TribeContext
import com.zegreatrob.coupling.stubmodel.stubUser

class SdkTribeContext<T> (
    override val sdk: Sdk,
    override val repository: T,
    override val tribeId: PartyId,
    override val clock: MagicClock
) : TribeContext<T>, Sdk by sdk {
    override val user = stubUser().copy(email = primaryAuthorizedUsername)
    override suspend fun perform(query: UserQuery): User? = sdk.perform(query)
    override suspend fun requestSpin(partyId: PartyId, players: List<Player>, pins: List<Pin>) =
        sdk.requestSpin(partyId, players, pins)
}