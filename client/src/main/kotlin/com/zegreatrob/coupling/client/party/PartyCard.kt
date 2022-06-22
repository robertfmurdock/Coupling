package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.Paths.currentPairsPage
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.gravatar.GravatarOptions
import com.zegreatrob.coupling.client.gravatar.gravatarImage
import com.zegreatrob.coupling.client.visuallyHidden
import com.zegreatrob.coupling.components.pngPath
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.Auto
import csstype.BoxShadow
import csstype.Color
import csstype.Display
import csstype.Flex
import csstype.LineStyle
import csstype.Margin
import csstype.NamedColor
import csstype.None
import csstype.PropertiesBuilder
import csstype.TextAlign
import csstype.integer
import csstype.number
import csstype.px
import csstype.rgba
import emotion.react.css
import react.ChildrenBuilder
import react.dom.aria.ariaHidden
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.router.dom.Link

data class PartyCard(val party: Party, val size: Int = 150) : DataPropsBind<PartyCard>(partyCard)

private val styles = useStyles("party/PartyCard")

val partyCard = tmFC<PartyCard> { (party, size) ->
    Link {
        to = party.id.currentPairsPage()
        visuallyHidden { +"Tribe Home Page" }
        span {
            css(styles.className) {
                staticCardStyles()
                partyCardCss(size)
            }
            asDynamic()["data-tribe-id"] = party.id.value
            ariaHidden = true

            div {
                css { margin = ((size * 0.02).px) }
                add(PartyCardHeader(party, size))
                partyGravatar(party, size)
            }
        }
    }
}

private fun PropertiesBuilder.staticCardStyles() {
    display = Display.inlineBlock
    borderStyle = LineStyle.outset
    borderColor = Color("#f5f5f5")
    backgroundColor = NamedColor.lightsteelblue
    textAlign = TextAlign.center
    textDecoration = None.none
    boxShadow = BoxShadow(0.px, 1.px, 3.px, rgba(0, 0, 0, 0.6))
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

val noPartyImagePath = pngPath("tribes/no-tribe")

private fun ChildrenBuilder.partyGravatar(party: Party, size: Int) = if (party.imageURL != null) {
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
        options = object : GravatarOptions {
            override val size = size
            override val default = "identicon"
        }
    )
}
