package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.json.JsonContributionWindow
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.sdk.gql.graphQuery
import js.objects.jso
import react.router.dom.SetURLSearchParams
import react.router.dom.useSearchParams

val IncubatingStatisticsPage = partyPageFunction { props, partyId ->
    val window: JsonContributionWindow? = props.search["window"]?.let { window ->
        JsonContributionWindow.entries.find { it.name == window }
    }
    val (_, setSearchParams) = useSearchParams()
    val setWindow = setWindowSearchParamHandler(setSearchParams)
    CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                details()
                playerList()
                pairs {
                    players()
                    contributions(window = window)
                }
            }
        },
        toNode = { reload, _, queryResult ->
            val party = queryResult.party ?: return@CouplingQuery null
            IncubatingPartyStatisticsContent.create(
                party = party.details?.data ?: return@CouplingQuery null,
                players = party.playerList?.elements ?: return@CouplingQuery null,
                pairs = party.pairs ?: return@CouplingQuery null,
                window = window,
                setWindow = {
                    setWindow(it)
                    reload()
                },
            )
        },
        key = partyId.value,
    )
}

private fun setWindowSearchParamHandler(setSearchParams: SetURLSearchParams) =
    { updatedWindow: JsonContributionWindow? ->
        setSearchParams({ previous ->
            previous.also {
                if (updatedWindow != null) {
                    previous["window"] = updatedWindow.name
                } else {
                    previous.delete("window")
                }
            }
        }, jso { })
    }
