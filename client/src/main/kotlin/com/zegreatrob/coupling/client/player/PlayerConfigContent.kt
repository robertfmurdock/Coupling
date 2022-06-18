package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.ConfigForm
import com.zegreatrob.coupling.client.ConfigFrame
import com.zegreatrob.coupling.client.ConfigHeader
import com.zegreatrob.coupling.client.Editor
import com.zegreatrob.coupling.client.external.react.configInput
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactrouter.PromptComponent
import com.zegreatrob.coupling.client.gravatarLink
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.create
import com.zegreatrob.minreact.tmFC
import csstype.Border
import csstype.ClassName
import csstype.LineStyle.Companion.outset
import csstype.NamedColor
import csstype.px
import react.ChildrenBuilder
import react.dom.events.ChangeEvent
import react.dom.html.InputType
import react.dom.html.ReactHTML.datalist
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.span
import react.key

data class PlayerConfigContent(
    val party: Party,
    val player: Player,
    val players: List<Player>,
    val onChange: (ChangeEvent<*>) -> Unit,
    val onSubmit: () -> Unit,
    val onRemove: () -> Unit
) : DataPropsBind<PlayerConfigContent>(playerConfigContent)

private val styles = useStyles("player/PlayerConfigEditor")
private val playerConfigStyles = useStyles("player/PlayerConfig")

val playerConfigContent = tmFC<PlayerConfigContent> { (party, player, players, onChange, onSubmit, onRemove) ->
    ConfigFrame {
        className = playerConfigStyles.className
        span {
            className = styles.className
            ConfigHeader {
                this.party = party
                +"Player Configuration"
            }
            div {
                div {
                    className = styles["player"]
                    playerConfigForm(player, party, onChange, onSubmit, onRemove)
//                    promptOnExit(shouldShowPrompt = updatedPlayer != player)
                }
                +PlayerCard(player, size = 250).create()
            }
        }
        div {
            +PlayerRoster(players = players, partyId = party.id) {
                display = csstype.Display.inlineBlock
                borderRadius = 20.px
                padding = 10.px
                border = Border(11.px, outset, NamedColor.tan)
                backgroundColor = NamedColor.wheat
            }.create()
        }
    }
}

private fun ChildrenBuilder.promptOnExit(shouldShowPrompt: Boolean) = PromptComponent {
    `when` = shouldShowPrompt
    message = "You have unsaved data. Press OK to leave without saving."
}

private fun ChildrenBuilder.playerConfigForm(
    player: Player,
    party: Party,
    onChange: (ChangeEvent<*>) -> Unit,
    onSubmit: () -> Unit,
    onRemoveFunc: (() -> Unit)?
) = ConfigForm {
    this.onSubmit = onSubmit
    this.onRemove = onRemoveFunc
    editorDiv(party, player, onChange)
}

private fun ChildrenBuilder.editorDiv(party: Party, player: Player, onChange: (ChangeEvent<*>) -> Unit) = div {
    Editor {
        li { nameInput(player, onChange) }
        li { emailInput(player, onChange) }
        if (party.callSignsEnabled) {
            callSignConfig(player, onChange)
        }
        if (party.badgesEnabled) {
            badgeConfig(party, player, onChange, styles["badgeConfig"])
        }
    }
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
        autoFocus = true
    )
    span { +"What's your moniker?" }
}

private fun ChildrenBuilder.emailInput(player: Player, onChange: (ChangeEvent<*>) -> Unit) {
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
    party: Party,
    player: Player,
    onChange: (ChangeEvent<*>) -> Unit,
    className: ClassName
) = li {
    this.className = className
    label { htmlFor = "badge"; +"Badge" }
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

private fun ChildrenBuilder.altBadgeOption(party: Party) = option {
    id = "alt-badge-option"
    key = "${Badge.Alternate.value}"
    value = "${Badge.Alternate.value}"
    label = party.alternateBadgeName
}

private fun ChildrenBuilder.defaultBadgeOption(party: Party) = option {
    id = "default-badge-option"
    key = "${Badge.Default.value}"
    value = "${Badge.Default.value}"
    label = party.defaultBadgeName
}
