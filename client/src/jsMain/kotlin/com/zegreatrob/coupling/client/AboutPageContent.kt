package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.CouplingImages
import com.zegreatrob.coupling.client.components.CouplingLink
import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.client.components.blue
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.minreact.nfc
import emotion.react.css
import kotools.types.text.toNotBlankString
import react.Key
import react.Props
import react.PropsWithChildren
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div
import web.cssom.AlignItems
import web.cssom.ClassName
import web.cssom.Color
import web.cssom.Display
import web.cssom.FlexWrap
import web.cssom.JustifyContent
import web.cssom.deg
import web.cssom.em
import web.cssom.px

val aboutPageContent by nfc<PropsWithChildren> { props ->
    div {
        PageFrame(
            borderColor = Color("rgb(94, 84, 102)"),
            backgroundColor = Color("floralwhite"),
        ) {
            div {
                css { width = 40.em }
                aboutHeader()
                +props.children
                playerHeader()
            }
        }
    }
}

private val aboutHeader by nfc<Props> {
    div {
        css {
            display = Display.flex
            justifyContent = JustifyContent.spaceBetween
            alignItems = AlignItems.center
            flexWrap = FlexWrap.wrap
            gap = 16.px
            marginRight = (-15).px
        }
        ReactHTML.h1 { +"About This App" }
        backButton()
    }
}

private val backButton by nfc<Props> {
    CouplingLink {
        to = "/parties"
        sizeRuleSet = large
        colorRuleSet = blue
        ReactHTML.i { className = ClassName("fa fa-step-backward") }
        ReactHTML.span { +"Back to Coupling!" }
    }
}

private val playerHeader by nfc<Props> {
    div {
        PlayerCard(
            defaultPlayer.copy(
                id = PlayerId("1".toNotBlankString().getOrThrow()),
                name = "RoB",
                imageURL = CouplingImages.images.robPng,
            ),
            tilt = (-8).deg,
            key = Key("1"),
        )
        PlayerCard(
            defaultPlayer.copy(
                id = PlayerId("2".toNotBlankString().getOrThrow()),
                name = "Autumn",
                imageURL = CouplingImages.images.autumnPng,
            ),
            tilt = 8.deg,
            key = Key("2"),
        )
    }
}
