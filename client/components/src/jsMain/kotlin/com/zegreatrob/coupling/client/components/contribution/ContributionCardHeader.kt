package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.TiltedPlayerList
import com.zegreatrob.coupling.client.components.format
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.client.components.pngPath
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.model.player.emails
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.create
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import web.cssom.AlignItems
import web.cssom.BackgroundRepeat
import web.cssom.Color
import web.cssom.Display
import web.cssom.FlexDirection
import web.cssom.FontWeight
import web.cssom.LineStyle
import web.cssom.Margin
import web.cssom.Overflow
import web.cssom.Position
import web.cssom.TextWrap
import web.cssom.VerticalAlign
import web.cssom.deg
import web.cssom.em
import web.cssom.integer
import web.cssom.pct
import web.cssom.px
import web.cssom.rgb
import web.cssom.rotatex
import web.cssom.scale
import web.cssom.translate
import web.cssom.url
import web.html.HTMLElement
import kotlin.uuid.Uuid

external interface ContributionCardHeaderProps : Props {
    @Suppress("INLINE_CLASS_IN_EXTERNAL_DECLARATION_WARNING")
    var partyId: PartyId
    var contribution: Contribution
    var players: List<Player>
    var onPlayerClick: ((Player, HTMLElement) -> Unit)?
}

@ReactFunc
val ContributionCardHeader by nfc<ContributionCardHeaderProps> { props ->
    val (partyId, contribution, players) = props
    val shortId = contribution.id.asShortId()
    val contributionPlayerList = contribution.participantEmails.map { email ->
        props.players.find { it.emails.contains(email) }
            ?: defaultPlayer.copy(
                id = "${Uuid.random()}",
                name = email.substringBefore("@"),
                email = email,
            )
    }.toSet()

    div {
        css {
            margin = Margin(0.2.em, 0.px)
            height = 2.5.em
            verticalAlign = VerticalAlign.top
            overflow = Overflow.visible
            display = Display.flex
            alignItems = AlignItems.center
            flexDirection = FlexDirection.column
            position = Position.relative
            transform = scale(1.04)
            perspective = 30.em
            top = (-0.6).em
        }
        div {
            css {
                position = Position.absolute
                overflow = Overflow.hidden
                borderRadius = 1.em
                top = 0.px
                left = 0.px
                right = 0.px
                bottom = 0.px
                transform = rotatex(20.deg)
                backgroundColor = rgb(255, 255, 255, 0.4)
                backgroundImage = url(pngPath("overlay"))
                backgroundRepeat = BackgroundRepeat.repeatX
                borderStyle = LineStyle.hidden
                borderColor = Color("#00000054")
                borderWidth = 1.px
                fontWeight = FontWeight.bold
            }
        }
        div {
            css {
                height = 1.3.em
                zIndex = integer(100)
                position = Position.absolute
                top = 50.pct
                left = 50.pct
                transform = translate((-50).pct, (-50).pct)
                fontSize = 1.4.em
            }
            div {
                css {
                    display = Display.flex
                    alignItems = AlignItems.center
                    height = 1.4.em
                    textWrap = TextWrap.nowrap
                }
                +"${contribution.label} $shortId ${contribution.dateTime?.format()}"
            }
        }
        div {
            css {
                position = Position.absolute
                right = if (contribution.link != null) 2.5.em else 5.px
                top = (-0.5).em
            }
            TiltedPlayerList(
                playerList = contributionPlayerList,
                element = { tilt, player ->
                    span.create {
                        onClick = { props.onPlayerClick?.invoke(player, it.currentTarget) }
                        PlayerCard(player = player, tilt = tilt, size = 30, key = player.id)
                    }
                },
            )
        }
        contribution.link?.let { link ->
            div {
                css {
                    position = Position.absolute
                    right = 5.px
                }
                ContributionLinkButton(link = link)
            }
        }
    }
}
