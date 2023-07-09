package com.zegreatrob.coupling.client.integration

import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.CouplingQueryResult
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.react.dataloader.ReloadFunc

val IntegrationPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                party()
                integration()
            }
            addToSlackUrl()
        },
        toNode = { _: ReloadFunc, _: DispatchFunc<CommandDispatcher>, result: CouplingQueryResult ->
            IntegrationContent.create(
                party = result.party?.details?.data ?: return@CouplingQuery null,
                integration = result.party?.integration?.data,
                addToSlackUrl = result.addToSlackUrl ?: return@CouplingQuery null,
            )
        },
        key = partyId.value,
    )
}
