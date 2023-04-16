package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.client.components.PlayerCard
import com.zegreatrob.coupling.client.components.external.reactmarkdown.Markdown
import com.zegreatrob.coupling.client.components.loadMarkdownString
import com.zegreatrob.coupling.client.components.welcome.playerImage
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc
import csstype.ClassName
import csstype.Color
import csstype.Position
import csstype.deg
import csstype.em
import csstype.px
import emotion.react.css
import react.Props
import react.PropsWithChildren
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span
import react.router.dom.Link

val AboutPage by nfc<PageProps> {
    aboutPageContent {
        Markdown { +loadMarkdownString("About") }
    }
}

val aboutPageContent by nfc<PropsWithChildren> { props ->
    div {
        add(PageFrame(borderColor = Color("rgb(94, 84, 102)"), backgroundColor = Color("floralwhite"))) {
            div {
                css { width = 40.em }
                backButtonSection()
                +props.children
                playerHeader()
            }
        }
    }
}

private val backButtonSection by nfc<Props> {
    div {
        css { position = Position.relative }
        ReactHTML.span {
            css { float = csstype.Float.left; position = Position.absolute; right = (-15).px; top = 20.px }
            backButton()
        }
    }
}

private val backButton by nfc<Props> {
    Link {
        to = "/parties"
        tabIndex = -1
        draggable = false
        add(
            com.zegreatrob.coupling.client.components.CouplingButton(
                com.zegreatrob.coupling.client.components.large,
                com.zegreatrob.coupling.client.components.blue,
                ClassName(""),
                {},
            ),
        ) {
            i { className = ClassName("fa fa-step-backward") }
            span { +"Back to Coupling!" }
        }
    }
}

private val playerHeader by nfc<Props> {
    div {
        val rob by playerImage()
        add(
            PlayerCard(Player("1", name = "RoB", imageURL = rob, avatarType = null), tilt = (-8).deg),
            key = "1",
        )
        val autumn by playerImage()
        add(
            PlayerCard(Player("2", name = "Autumn", imageURL = autumn, avatarType = null), tilt = 8.deg),
            key = "2",
        )
    }
}
