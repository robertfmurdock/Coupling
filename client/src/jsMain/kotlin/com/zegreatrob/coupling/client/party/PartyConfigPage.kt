package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.components.party.PartyConfig
import com.zegreatrob.coupling.client.gql.PartyQuery
import com.zegreatrob.coupling.client.gql.UserQuery
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.partyId
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import com.zegreatrob.minreact.nfc
import js.lazy.Lazy
import react.Key
import kotlin.uuid.Uuid

@Lazy
val PartyConfigPage by nfc<PageProps> { props ->
    val partyId = props.partyId
    if (partyId != null) {
        CouplingQuery(
            commander = props.commander,
            query = GqlQuery(PartyQuery(partyId)),
            key = Key(partyId.value.toString()),
        ) { _, commandFunc, result ->
            PartyConfig(
                party = result.party?.partyDetails?.toDomain() ?: return@CouplingQuery,
                boost = null,
                dispatchFunc = commandFunc,
                isNew = false,
            )
        }
    } else {
        CouplingQuery(
            commander = props.commander,
            query = GqlQuery(UserQuery()),
        ) { _, commandFunc, _ -> PartyConfig(newParty(), null, commandFunc, isNew = true) }
    }
}

fun newParty() = PartyDetails(
    id = PartyId(Uuid.random().toString()),
    defaultBadgeName = "Default",
    alternateBadgeName = "Alternate",
    name = "New Party",
)
