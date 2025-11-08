package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin

fun Record<PartyElement<Pin>>.toSerializable() = GqlPin(
    id = data.element.id,
    name = data.element.name,
    icon = data.element.icon,
    partyId = data.partyId,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun GqlPin.toModel(): Record<PartyElement<Pin>> = Record(
    data = partyId.with(
        Pin(
            id = id,
            name = name,
            icon = icon,
        ),
    ),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = timestamp,
)
