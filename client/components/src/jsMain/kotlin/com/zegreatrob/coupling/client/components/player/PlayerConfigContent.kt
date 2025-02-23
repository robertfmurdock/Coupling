package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.client.components.ConfigForm
import com.zegreatrob.coupling.client.components.ConfigFrame
import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.Editor
import com.zegreatrob.coupling.client.components.configInput
import com.zegreatrob.coupling.client.components.gravatarLink
import com.zegreatrob.coupling.client.components.small
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.AvatarType
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.emails
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import csstype.PropertiesBuilder
import emotion.react.css
import org.w3c.dom.HTMLInputElement
import react.ChildrenBuilder
import react.Props
import react.dom.events.ChangeEvent
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.datalist
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.span
import web.cssom.Border
import web.cssom.ClassName
import web.cssom.Clear
import web.cssom.Display
import web.cssom.FontSize
import web.cssom.LineStyle
import web.cssom.NamedColor
import web.cssom.None
import web.cssom.Position
import web.cssom.TextAlign
import web.cssom.VerticalAlign
import web.cssom.number
import web.cssom.px
import web.html.InputType

val playerConfigContentClassName = ClassName("player-config-content")

external interface PlayerConfigContentProps : Props {
    var party: PartyDetails
    var boost: Boost?
    var player: Player
    var players: List<Player>
    var onChange: (ChangeEvent<*>) -> Unit
    var onSubmit: () -> Unit
    var onRemove: (() -> Unit)?
    var onPlayerChange: (Player) -> Unit
}

@ReactFunc
val PlayerConfigContent by nfc<PlayerConfigContentProps> { props ->
    val (party, boost, player, players, onChange, onSubmit, onRemove, onPlayerChange) = props

    ConfigFrame {
        css(playerConfigContentClassName) {
            "input[type=text]" {
                fontSize = FontSize.large
                borderRadius = 20.px
                borderWidth = 4.px
                padding = 4.px
            }
        }
        span {
            ConfigHeader(party = party, boost = boost) {
                +"Player Configuration"
            }
            div {
                div {
                    css {
                        verticalAlign = VerticalAlign.top
                        flexGrow = number(2.0)
                        position = Position.relative
                        clear = Clear.both
                        display = Display.inlineBlock
                        textAlign = TextAlign.center
                        textDecoration = None.none
                        color = NamedColor.black
                    }
                    playerConfigForm(player, party, onChange, onSubmit, onRemove, onPlayerChange)
                }
                PlayerCard(player, size = 250)
            }
        }
        div {
            PlayerRoster(players = players, partyId = party.id, cssOverrides = fun PropertiesBuilder.() {
                display = Display.inlineBlock
                borderRadius = 20.px
                padding = 10.px
                border = Border(11.px, LineStyle.outset, NamedColor.tan)
                backgroundColor = NamedColor.wheat
            })
        }
    }
}

private fun ChildrenBuilder.playerConfigForm(
    player: Player,
    party: PartyDetails,
    onChange: (ChangeEvent<*>) -> Unit,
    onSubmit: () -> Unit,
    onRemoveFunc: (() -> Unit)?,
    onPlayerChange: (Player) -> Unit,
) = ConfigForm(
    onSubmit = onSubmit,
    onRemove = onRemoveFunc,
) {
    div {
        Editor {
            li { nameInput(player, onChange) }
            li { emailInput(player, onChange) }
            val additionalEmailsList = player.additionalEmails.toList()
            additionalEmailsList.forEachIndexed { index, email ->
                li {
                    additionalEmailInput(
                        index = index + 2,
                        onChange = onAdditionalEmailChange(onPlayerChange, player, additionalEmailsList, index),
                        email = email,
                    )
                }
            }
            if (!player.emails.contains("")) {
                li {
                    CouplingButton {
                        sizeRuleSet = small
                        onClick = { onPlayerChange(player.copy(additionalEmails = player.additionalEmails + "")) }
                        +"Add Additional Email"
                    }
                }
            }
            avatarTypeConfig(player, onChange)
            if (party.callSignsEnabled) {
                callSignConfig(player, onChange)
            }
            if (party.badgesEnabled) {
                badgeConfig(party, player, onChange)
            }
        }
    }
}

private fun onAdditionalEmailChange(
    onPlayerChange: (Player) -> Unit,
    player: Player,
    additionalEmailsList: List<String>,
    index: Int,
): (ChangeEvent<*>) -> Unit = {
    val changeValue = (it.target.unsafeCast<HTMLInputElement>()).value
    onPlayerChange(
        player.copy(
            additionalEmails = additionalEmailsList.updateEmailAtIndex(index, changeValue),
        ),
    )
}

private fun List<String>.updateEmailAtIndex(index: Int, changeValue: String) = mapIndexed { emailIndex, value ->
    if (emailIndex == index) {
        changeValue
    } else {
        value
    }
}.toSet()

private fun ChildrenBuilder.additionalEmailInput(
    index: Int,
    onChange: (ChangeEvent<*>) -> Unit,
    email: String,
) {
    configInput(
        labelText = "Email $index",
        id = "player-email-$index",
        name = "email-$index",
        value = email,
        type = InputType.text,
        onChange = onChange,
        placeholder = "email-$index",
    )
}

private fun ChildrenBuilder.nameInput(player: Player, onChange: (ChangeEvent<*>) -> Unit) {
    configInput(
        labelText = "Name",
        id = "player-name",
        name = "name",
        value = player.name,
        type = InputType.text,
        onChange = onChange,
        placeholder = "My name is...",
        autoFocus = true,
    )
    span { +"What's your moniker?" }
}

private fun ChildrenBuilder.emailInput(
    player: Player,
    onChange: (ChangeEvent<*>) -> Unit,
) {
    configInput(
        labelText = "Email",
        id = "player-email",
        name = "email",
        value = player.email,
        type = InputType.text,
        onChange = onChange,
        placeholder = "email",
    )
    span {
        div { +"Email provides access privileges;" }
        div { +"That means if someone with that email logs into Coupling, they can see their party!" }
        div {
            +"To change your player picture, assign a"
            gravatarLink()
            +"to this email."
        }
    }
}

private fun ChildrenBuilder.callSignConfig(player: Player, onChange: (ChangeEvent<*>) -> Unit) {
    li {
        configInput(
            labelText = "Call-Sign Adjective",
            id = "adjective-input",
            name = "callSignAdjective",
            value = player.callSignAdjective,
            type = InputType.text,
            onChange = onChange,
            list = "callSignAdjectiveOptions",
        )
        datalist { id = "callSignAdjectiveOptions" }
        span { +"I feel the need..." }
    }
    li {
        configInput(
            labelText = "Call-Sign Noun",
            id = "noun-input",
            name = "callSignNoun",
            value = player.callSignNoun,
            type = InputType.text,
            onChange = onChange,
            list = "callSignNounOptions",
        )
        datalist { id = "callSignNounOptions" }
        span { +"... the need for speed!" }
    }
}

private fun ChildrenBuilder.badgeConfig(
    party: PartyDetails,
    player: Player,
    onChange: (ChangeEvent<*>) -> Unit,
) = li {
    css {
        "> div" {
            display = Display.inlineBlock
        }
    }
    label {
        htmlFor = "badge"
        +"Badge"
    }
    select {
        id = "badge"
        name = "badge"
        this.value = "${player.badge}"
        this.onChange = onChange
        defaultBadgeOption(party)
        altBadgeOption(party)
    }
    span { +"Your badge makes you feel... different than the others." }
}

private fun ChildrenBuilder.altBadgeOption(party: PartyDetails) = option {
    id = "alt-badge-option"
    key = "${Badge.Alternate.value}"
    value = "${Badge.Alternate.value}"
    label = party.alternateBadgeName
    ariaLabel = "Alt Badge Option"
}

private fun ChildrenBuilder.defaultBadgeOption(party: PartyDetails) = option {
    id = "default-badge-option"
    key = "${Badge.Default.value}"
    value = "${Badge.Default.value}"
    label = party.defaultBadgeName
    ariaLabel = "Default Badge Option"
}

private fun ChildrenBuilder.avatarTypeConfig(
    player: Player,
    onChange: (ChangeEvent<*>) -> Unit,
) = li {
    css {
        "> div" {
            display = Display.inlineBlock
        }
    }
    label {
        htmlFor = "avatarType"
        +"Avatar Type"
    }
    select {
        id = "avatarType"
        name = "avatarType"
        this.value = player.avatarType?.name ?: ""
        this.onChange = onChange
        option {
            value = ""
            this.label = "No avatar currently selected."
        }
        AvatarType.entries.forEach { avatarType ->
            option {
                id = avatarType.name
                key = avatarType.name
                value = avatarType.name
                this.label = avatarType.name
                ariaLabel = avatarType.name
            }
        }
    }
    span {
        div { +"You may feel a sudden urge to paint yourself blue." }
        player.avatarType?.let {
            it.attribution?.let { attribution ->
                div { +"${it.name} comes from " }
                div {
                    a {
                        href = attribution
                        +attribution
                    }
                }
                div { +"Send them love if you like!" }
            }
        }
    }
}
