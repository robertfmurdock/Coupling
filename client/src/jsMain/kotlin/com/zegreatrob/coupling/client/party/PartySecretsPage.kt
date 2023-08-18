package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.components.ConfigForm
import com.zegreatrob.coupling.client.components.ConfigFrame
import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.client.components.party.PartyCard
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.navigateToPartyList
import com.zegreatrob.coupling.client.routing.partyId
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div
import web.cssom.Color
import web.cssom.Display
import web.cssom.number

val PartySecretsPage by nfc<PageProps> { props ->
    val partyId = props.partyId
    if (partyId != null) {
        CouplingQuery(
            commander = props.commander,
            query = graphQuery {
                party(partyId) {
                    details()
                    secretList()
                    boost()
                }
            },
            toNode = { _, _, result ->
                PartySecretLayout.create(
                    partyDetails = result.party?.details?.data ?: return@CouplingQuery null,
                    secrets = result.party?.secretList?.elements ?: emptyList(),
                    boost = result.party?.boost?.data,
                )
            },
            key = props.partyId?.value,
        )
    } else {
        +navigateToPartyList()
    }
}

@ReactFunc
external interface PartySecretsLayoutProps : Props {
    var partyDetails: PartyDetails
    var secrets: List<Secret>
    var boost: Boost?
}

val PartySecretLayout by nfc<PartySecretsLayoutProps> { props ->
    val party = props.partyDetails
    ConfigFrame {
        backgroundColor = Color("hsla(45, 80%, 96%, 1)")
        borderColor = Color("#ff8c00")
        ConfigHeader(party = party, boost = props.boost) {
            +"Party Secrets"
        }
        div {
            css { display = Display.flex }
            ReactHTML.span {
                css {
                    display = Display.inlineBlock
                    flexGrow = number(2.0)
                }
                ConfigForm {
                    +"Secret List"
                    props.secrets.forEach {
                        div {
                            +"Secret: ${it.id}"
                        }
                    }
                }
            }
            PartyCard(party)
        }
    }
}
