package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin
import kotools.types.text.NotBlankString
import kotools.types.text.toNotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi

fun Record<PartyElement<Pin>>.toSerializable() = GqlPinDetails(
    id = "${data.element.id}",
    name = data.element.name,
    icon = data.element.icon,
    partyId = data.partyId.value.toString(),
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

@OptIn(ExperimentalKotoolsTypesApi::class)
fun GqlPinDetails.toModel(): Record<PartyElement<Pin>>? {
    return Record(
        data = PartyId(NotBlankString.create(partyId)).with(
            Pin(
                id = id.toNotBlankString().getOrNull() ?: return null,
                name = name,
                icon = icon,
            ),
        ),
        modifyingUserId = modifyingUserEmail,
        isDeleted = isDeleted,
        timestamp = timestamp,
    )
}
