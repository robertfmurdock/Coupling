package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.blue
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.loadMarkdownString
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactmarkdown.Markdown
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.child
import kotlinx.css.*
import react.FC
import react.RBuilder
import react.buildElement
import react.create
import react.dom.html.ReactHTML.div
import react.dom.i
import react.dom.span
import react.router.dom.Link
import styled.css
import styled.styledDiv
import styled.styledSpan

private val styles = useStyles("About")

val AboutPage = FC<PageProps> {
    div {
        className = styles.className
        div {
            className = styles["content"]
            child(backButtonSection())
            Markdown { +loadMarkdownString("About") }
            child(playerHeader())
        }
    }
}

private fun backButtonSection() = buildElement {
    styledDiv {
        css { position = Position.relative }
        styledSpan {
            css { float = Float.left; position = Position.absolute; right = (-15).px; top = 20.px }
            child(backButton())
        }
    }
}

private fun backButton() = Link.create {
    to = "/tribes"
    child(CouplingButton(large, blue, "", {}, {}, fun RBuilder.() {
        i(classes = "fa fa-step-backward") {}
        span { +"Back to Coupling!" }
    }))
}

private fun playerHeader() = div.create {
    val tribeId = TribeId("developers")
    val rob by playerImage()
    val autumn by playerImage()

    listOf(
        "left" to Player("1", name = "RoB", imageURL = rob),
        "right" to Player("2", name = "Autumn", imageURL = autumn)
    ).forEach { (side, player) ->
        child(PlayerCard(tribeId, player, className = playerCardStyles(side)))
    }
}

private fun playerCardStyles(sideClassName: String) = listOf(
    styles["player"],
    styles[sideClassName]
).joinToString(" ")
