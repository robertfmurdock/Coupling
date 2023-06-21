package com.zegreatrob.coupling.client.slack

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.client.components.ConfigForm
import com.zegreatrob.coupling.client.components.ConfigFrame
import com.zegreatrob.coupling.client.components.Editor
import com.zegreatrob.coupling.client.components.GqlButton
import com.zegreatrob.coupling.client.components.LogoutButton
import com.zegreatrob.coupling.client.create
import com.zegreatrob.coupling.client.party.CouplingLogo
import com.zegreatrob.coupling.client.party.GeneralControlBar
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.DataPropsBridge
import com.zegreatrob.minreact.nfc
import com.zegreatrob.minreact.ntmFC
import react.FC
import react.create
import react.dom.aria.ariaLabel
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.span
import react.useMemo
import web.cssom.Color

val SlackConnectPage by nfc<PageProps> { props ->
    +CouplingQuery(
        commander = props.commander,
        query = graphQuery { partyList() },
        toDataprops = { _, _, result ->
            println("query response is $result")
            SlackConnectContent(
                result.partyList?.map(Record<PartyDetails>::data) ?: emptyList(),
            )
        },
    ).create()
}

data class SlackConnectContent(val parties: List<PartyDetails>) : DataPropsBind<SlackConnectContent>(
    slackConnectContent,
)

val slackConnectContent: FC<DataPropsBridge> by ntmFC<SlackConnectContent> { (parties) ->
    ConfigFrame {
        backgroundColor = Color("hsla(45, 80%, 96%, 1)")
        borderColor = Color("#ff8c00")
        GeneralControlBar {
            title = "Slack Connect"
            splashComponent = CouplingLogo.create {
                width = 72.0
                height = 48.0
            }
            LogoutButton()
            GqlButton()
        }

        val partySelectId = useMemo { "${uuid4()}" }
        ConfigForm {
            div {
                Editor {
                    li {
                        label {
                            htmlFor = partySelectId
                            +"Party"
                        }
                        select {
                            id = partySelectId
                            name = "party"
                            value = null
                            onChange = { }
                            parties.map { party ->
                                val partyName = party.name
                                option {
                                    key = party.id.value
                                    value = partyName
                                    label = partyName
                                    if (partyName != null) {
                                        ariaLabel = partyName
                                    }
                                }
                            }
                        }
                        span {
                            +"Which party would you like to connect to a Slack channel?"
                        }
                    }
                }
            }
        }
    }
}
