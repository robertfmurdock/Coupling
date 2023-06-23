package com.zegreatrob.coupling.client.incubating

import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc
import com.zegreatrob.react.dataloader.DataLoader
import com.zegreatrob.react.dataloader.EmptyState
import com.zegreatrob.react.dataloader.PendingState
import com.zegreatrob.react.dataloader.ResolvedState
import react.Props
import react.dom.html.ReactHTML.div
import web.cssom.Color

val IncubatingPage by nfc<Props> {
    add(PageFrame(borderColor = Color("#e8e8e8"), backgroundColor = Color("#dcd9d9"))) {
        add(
            DataLoader({ "" }, { "" }) { state ->
                +"Incubating Features - Best not to touch"
                div {
                    when (state) {
                        is EmptyState -> {}
                        is PendingState -> {}
                        is ResolvedState -> AddToSlackButton { url = state.result }
                    }
                }
            },
        )
    }
}
