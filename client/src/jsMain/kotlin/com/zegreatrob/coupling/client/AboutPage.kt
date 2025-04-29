package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.client.components.blue
import com.zegreatrob.coupling.client.components.external.marked.parse
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.loadMarkdownString
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.client.components.welcome.playerImage
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.minreact.nfc
import emotion.react.css
import js.objects.unsafeJso
import kotools.types.text.toNotBlankString
import react.Props
import react.PropsWithChildren
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span
import react.router.dom.Link
import web.cssom.ClassName
import web.cssom.Color
import web.cssom.Float
import web.cssom.Position
import web.cssom.deg
import web.cssom.em
import web.cssom.px

val AboutPage by nfc<PageProps> {
    aboutPageContent {
        div {
            dangerouslySetInnerHTML = unsafeJso {
                __html =
                    parse(loadMarkdownString("About"))
            }
        }
    }
}

val aboutPageContent by nfc<PropsWithChildren> { props ->
    div {
        PageFrame(
            borderColor = Color("rgb(94, 84, 102)"),
            backgroundColor = Color("floralwhite"),
        ) {
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
        span {
            css {
                float = Float.left
                position = Position.absolute
                right = (-15).px
                top = 20.px
            }
            backButton()
        }
    }
}

private val backButton by nfc<Props> {
    Link {
        to = "/parties"
        tabIndex = -1
        draggable = false
        CouplingButton {
            sizeRuleSet = large
            colorRuleSet = blue
            onClick = {}
            i { className = ClassName("fa fa-step-backward") }
            span { +"Back to Coupling!" }
        }
    }
}

private val playerHeader by nfc<Props> {
    div {
        val rob by playerImage()
        PlayerCard(
            defaultPlayer.copy(PlayerId("1".toNotBlankString().getOrThrow()), name = "RoB", imageURL = rob),
            tilt = (-8).deg,
            key = "1",
        )
        val autumn by playerImage()
        PlayerCard(
            defaultPlayer.copy(
                PlayerId("2".toNotBlankString().getOrThrow()),
                name = "Autumn",
                imageURL = autumn,
            ),
            tilt = 8.deg,
            key = "2",
        )
    }
}
