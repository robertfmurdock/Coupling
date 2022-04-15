package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.ConfigHeader
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.tmFC
import react.dom.html.ReactHTML.div

data class PartyBrowser(val party: Party) : DataPropsBind<PartyBrowser>(partyBrowser)

private val styles = useStyles("party/TribeBrowser")

val partyBrowser = tmFC<PartyBrowser> { (party) ->
    div {
        className = styles.className
        ConfigHeader {
            this.party = party
            +(party.name ?: "")
        }
    }
}
