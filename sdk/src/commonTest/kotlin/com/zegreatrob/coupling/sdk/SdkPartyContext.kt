package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.pairassignmentdocument.RequestSpinAction
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.user.UserQuery
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PartyContext
import com.zegreatrob.coupling.stubmodel.stubUser

class SdkPartyContext<T> (
    override val sdk: Sdk,
    override val repository: T,
    override val partyId: PartyId,
    override val clock: MagicClock,
) : PartyContext<T>, Sdk by sdk {
    override val user = stubUser().copy(email = primaryAuthorizedUsername)
    override suspend fun perform(query: UserQuery): User? = sdk.perform(query)
    override suspend fun perform(action: RequestSpinAction) = sdk.perform(action)
    override suspend fun perform(command: SavePlayerCommand) = sdk.perform(command)
}
