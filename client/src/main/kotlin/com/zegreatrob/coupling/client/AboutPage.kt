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
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span
import react.router.dom.Link

private val styles = useStyles("About")

val AboutPage = FC<PageProps> {
    div {
        className = styles.className
        div {
            className = styles["content"]
            backButtonSection()
            Markdown { +loadMarkdownString("About") }
            playerHeader()
        }
    }
}

private val backButtonSection = FC<Props> {
    cssDiv(css = { position = Position.relative }) {
        cssSpan(css = { float = Float.left; position = Position.absolute; right = (-15).px; top = 20.px }) {
            backButton()
        }
    }
}

private val backButton = FC<Props> {
    Link {
        to = "/tribes"
        tabIndex = -1
        child(CouplingButton(large, blue, "", {})) {
            i { className = "fa fa-step-backward" }
            span { +"Back to Coupling!" }
        }
    }
}

private val playerHeader = FC<Props> {
    div {
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
}

private fun playerCardStyles(sideClassName: String) = listOf(
    styles["player"],
    styles[sideClassName]
).joinToString(" ")
