package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.client.components.stats.MyResponsiveLine
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.dom.html.ReactHTML.div
import web.cssom.Color
import web.cssom.px

val GraphPage by nfc<PageProps> {
    div {
        PageFrame(borderColor = Color("#e8e8e8"), backgroundColor = Color("#dcd9d9")) {
            ConfigHeader {
                this.party = PartyDetails(PartyId(""))
                +"Graph Test Page"
            }
            div {
                css { height = 600.px }
                MyResponsiveLine()
            }
        }
    }
}
