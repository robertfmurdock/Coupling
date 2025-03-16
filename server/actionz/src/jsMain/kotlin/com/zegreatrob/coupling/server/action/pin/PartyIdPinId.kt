package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.model.party.PartyId
import kotools.types.text.NotBlankString

data class PartyIdPinId(val partyId: PartyId, val pinId: NotBlankString)
