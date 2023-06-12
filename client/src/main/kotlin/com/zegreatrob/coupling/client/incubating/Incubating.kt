package com.zegreatrob.coupling.client.incubating

import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML.div
import web.cssom.Color

val IncubatingPage by nfc<Props> {
    add(PageFrame(borderColor = Color("#e8e8e8"), backgroundColor = Color("#dcd9d9"))) {
        +"Incubating Features - Best not to touch"
        div {
            AddToSlackButton()
        }
    }
}
