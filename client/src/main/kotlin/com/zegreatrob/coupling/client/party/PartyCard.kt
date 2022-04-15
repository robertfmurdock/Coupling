package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.Paths.currentPairsPage
import com.zegreatrob.coupling.client.cssDiv
import com.zegreatrob.coupling.client.cssSpan
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.gravatar.GravatarOptions
import com.zegreatrob.coupling.client.gravatar.gravatarImage
import com.zegreatrob.coupling.client.pngPath
import com.zegreatrob.coupling.client.visuallyHidden
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import kotlinx.css.*
import kotlinx.html.classes
import react.ChildrenBuilder
import react.dom.aria.ariaHidden
import react.dom.html.ReactHTML
import react.router.dom.Link
import kotlin.collections.set

data class PartyCard(val party: Party, val size: Int = 150) : DataPropsBind<PartyCard>(partyCard)

private val styles = useStyles("party/TribeCard")

val partyCard = tmFC<PartyCard> { (party, size) ->
    Link {
        to = party.id.currentPairsPage()
        visuallyHidden { +"Tribe Home Page" }
        cssSpan(
            attrs = {
                classes = classes + styles.className.toString()
                attributes["data-tribe-id"] = party.id.value
                ariaHidden = true
            },
            css = partyCardCss(size)
        ) {
            cssDiv(css = { margin((size * 0.02).px) }) {
                child(PartyCardHeader(party, size))
                partyGravatar(party, size)
            }
        }
    }
}

private fun partyCardCss(size: Int): RuleSet = {
    val totalExtraMarginNeededForImage = 2 * (size * 0.02)
    width = (size + totalExtraMarginNeededForImage).px
    height = (size * 1.4).px
    borderWidth = (size * 0.01).px
    borderRadius = (size * 10.0 / 150).px
    flex(0.0, 0.0)
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