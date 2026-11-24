package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.CouplingLink
import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.client.components.blue
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.minreact.nfc
import emotion.react.css
import js.lazy.Lazy
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import web.cssom.Color
import web.cssom.Margin
import web.cssom.px

@Lazy
val DeveloperSettingsPage by nfc<PageProps> {
    PageFrame(
        borderColor = Color("rgb(94, 84, 102)"),
        backgroundColor = Color("floralwhite"),
    ) {
        div {
            css { margin = Margin(10.px, 10.px) }
            CouplingLink {
                to = "/parties/"
                sizeRuleSet = large
                colorRuleSet = blue
                +"Back to parties"
            }
        }
        h1 { +"Developer settings" }
        ClientLogHistoryControls()
    }
}
