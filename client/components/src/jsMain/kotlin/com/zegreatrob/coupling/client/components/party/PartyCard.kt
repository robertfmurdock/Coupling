package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.coupling.client.components.Paths.currentPairsPage
import com.zegreatrob.coupling.client.components.gravatar.gravatarImage
import com.zegreatrob.coupling.client.components.pin.PinButton
import com.zegreatrob.coupling.client.components.pin.PinButtonScale
import com.zegreatrob.coupling.client.components.pngPath
import com.zegreatrob.coupling.client.components.visuallyHidden
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import csstype.PropertiesBuilder
import emotion.react.css
import js.core.jso
import react.ChildrenBuilder
import react.Props
import react.dom.aria.ariaHidden
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.router.dom.Link
import web.cssom.AnimationIterationCount
import web.cssom.Auto
import web.cssom.BoxShadow
import web.cssom.Color
import web.cssom.Display
import web.cssom.Flex
import web.cssom.Float
import web.cssom.LineStyle
import web.cssom.Margin
import web.cssom.NamedColor
import web.cssom.None
import web.cssom.Position
import web.cssom.TextAlign
import web.cssom.ident
import web.cssom.integer
import web.cssom.number
import web.cssom.px
import web.cssom.rgb
import web.cssom.s

external interface PartyCardProps : Props {
    var party: PartyDetails
    var size: Int?
    var boost: Boost?
}

private val boostPin = Pin(name = "Boost!", icon = "fa-bolt-lightning")

@ReactFunc
val PartyCard by nfc<PartyCardProps> { props ->
    val (party) = props
    val size = props.size ?: 150
    Link {
        to = party.id.currentPairsPage()
        visuallyHidden { +"Party Home Page" }
        span {
            css {
                staticCardStyles()
                partyCardCss(size)
            }
            asDynamic()["data-party-id"] = party.id.value
            ariaHidden = true

            div {
                css { margin = ((size * 0.02).px) }
                PartyCardHeader(party, size)
                props.boost?.let { boostIndicator(it) }
                partyGravatar(party, size)
            }
        }
    }
}

private fun ChildrenBuilder.boostIndicator(boost: Boost) {
    span {
        css {
            float = Float.right
            zIndex = integer(100)
            position = Position.relative
            animationName = ident("pulsate")
            animationDuration = 0.75.s
            animationIterationCount = AnimationIterationCount.infinite
            hover {
                animationDuration = 0.25.s
            }
        }
        boostPinButton(boost)
    }
}

private fun ChildrenBuilder.boostPinButton(boost: Boost) {
    PinButton(
        pin = boostPin,
        scale = PinButtonScale.Small,
        tooltipMessage = "You've been boosted by user ${boost.userId}!",
    )
}

private fun PropertiesBuilder.staticCardStyles() {
    display = Display.inlineBlock
    borderStyle = LineStyle.outset
    borderColor = Color("#f5f5f5")
    backgroundColor = NamedColor.lightsteelblue
    textAlign = TextAlign.center
    textDecoration = None.none
    boxShadow = BoxShadow(0.px, 1.px, 3.px, rgb(0, 0, 0, 0.6))
    color = NamedColor.black
    margin = Margin(0.px, 2.px, 4.px, 2.px)

    "img" {
        zIndex = integer(0)
        asDynamic()["-webkit-filter"] = "drop-shadow(4px 4px 5px rgba(0, 0, 0, 0.5));"
    }
}

private fun PropertiesBuilder.partyCardCss(size: Int) {
    val totalExtraMarginNeededForImage = 2 * (size * 0.02)
    width = (size + totalExtraMarginNeededForImage).px
    height = (size * 1.4).px
    borderWidth = (size * 0.01).px
    borderRadius = (size * 10.0 / 150).px
    flex = Flex(number(0.0), number(0.0), Auto.auto)
}

val noPartyImagePath = pngPath("parties/no-party")

private fun ChildrenBuilder.partyGravatar(party: PartyDetails, size: Int) = if (party.imageURL != null) {
    ReactHTML.img {
        this.src = party.imageURL
        alt = "icon"
        this.width = size.toDouble()
        this.height = size.toDouble()
    }
} else {
    gravatarImage(
        email = party.email,
        alt = "party-img",
        fallback = noPartyImagePath,
        options = jso {
            this.size = size
            this.default = "identicon"
        },
    )
}
