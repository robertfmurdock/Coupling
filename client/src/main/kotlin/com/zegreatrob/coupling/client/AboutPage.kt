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
import com.zegreatrob.minreact.child
import csstype.ClassName
import csstype.Color
import csstype.Position
import csstype.em
import csstype.px
import emotion.react.css
import react.FC
import react.Props
import react.PropsWithChildren
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span
import react.router.dom.Link

private val styles = useStyles("About")

val AboutPage = FC<PageProps> {
    aboutPageContent {
        Markdown { +loadMarkdownString("About") }
    }
}

val aboutPageContent = FC<PropsWithChildren> { props ->
    div {
        className = styles.className
        child(PageFrame(borderColor = Color("rgb(94, 84, 102)"), backgroundColor = Color("floralwhite"))) {
            div {
                css { width = 40.em }
                backButtonSection()
                +props.children
                playerHeader()
            }
        }
    }
}

private val backButtonSection = FC<Props> {
    div {
        css { position = Position.relative }
        ReactHTML.span {
            css { float = csstype.Float.left; position = Position.absolute; right = (-15).px; top = 20.px }
            backButton()
        }
    }
}

private val backButton = FC<Props> {
    Link {
        to = "/tribes"
        tabIndex = -1
        draggable = false
        child(CouplingButton(large, blue, ClassName(""), {})) {
            i { className = ClassName("fa fa-step-backward") }
            span { +"Back to Coupling!" }
        }
    }
}

private val playerHeader = FC<Props> {
    div {
        val rob by playerImage()
        val autumn by playerImage()

        listOf(
            "left" to Player("1", name = "RoB", imageURL = rob),
            "right" to Player("2", name = "Autumn", imageURL = autumn)
        ).forEach { (side, player) ->
            child(PlayerCard(player, className = playerCardStyles(side)), key = player.id)
        }
    }
}

private fun playerCardStyles(sideClassName: String) = ClassName(
    listOf(
        styles["player"],
        styles[sideClassName]
    ).joinToString(" ")
)
