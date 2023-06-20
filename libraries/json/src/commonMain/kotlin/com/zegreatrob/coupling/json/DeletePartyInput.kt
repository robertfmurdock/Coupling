@file:UseSerializers(PartyIdSerializer::class)

package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.party.PartyId
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class DeletePartyInput(override val partyId: PartyId) : IPartyInput
