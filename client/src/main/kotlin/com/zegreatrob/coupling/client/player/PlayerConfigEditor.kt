package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.client.Paths.currentPairsPage
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.reactrouter.prompt
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.external.w3c.requireConfirmation
import com.zegreatrob.coupling.json.*
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.events.Event
import react.RBuilder
import react.dom.*
import react.router.Navigate
import react.useState
import kotlin.js.Json

data class PlayerConfigEditoProps(
    val tribe: Tribe,
    val player: Player,
    val reload: () -> Unit,
    val dispatchFunc: DispatchFunc<out PlayerConfigDispatcher>
) : DataProps

val PlayerConfigEditor by lazy { playerConfigEditor(WindowFunctions) }

private val styles = useStyles("player/PlayerConfigEditor")

val playerConfigEditor = windowReactFunc<PlayerConfigEditoProps> { props, windowFuncs ->
    val (tribe, player, reload, dispatchFunc) = props
    val (values, onChange) = useForm(player.toSerializable().toJsonDynamic().unsafeCast<Json>())

    val (redirectUrl, setRedirectUrl) = useState<String?>(null)

    val updatedPlayer = values.fromJsonDynamic<JsonPlayerData>().toModel()
    val onSubmit = dispatchFunc({ SavePlayerCommand(tribe.id, updatedPlayer) }, { reload() })
    val onRemove = dispatchFunc({ DeletePlayerCommand(tribe.id, player.id) },
        { setRedirectUrl(tribe.id.currentPairsPage()) })
        .requireConfirmation("Are you sure you want to delete this player?", windowFuncs)

    if (redirectUrl != null)
        Navigate { attrs.to = redirectUrl }
    else
        span(classes = styles.className) {
            configHeader(tribe) { +"Player Configuration" }
            div {
                div(classes = styles["player"]) {
                    playerConfigForm(updatedPlayer, tribe, onChange, onSubmit, onRemove)
//                    promptOnExit(shouldShowPrompt = updatedPlayer != player)
                }
                playerCard(PlayerCardProps(tribe.id, updatedPlayer, size = 250))
            }
        }
}

private fun RBuilder.promptOnExit(shouldShowPrompt: Boolean) = prompt(
    `when` = shouldShowPrompt,
    message = "You have unsaved data. Press OK to leave without saving."
)

private fun RBuilder.playerConfigForm(
    player: Player,
    tribe: Tribe,
    onChange: (Event) -> Unit,
    onSubmit: () -> Unit,
    onRemoveFunc: (() -> Unit)?
) = child(ConfigForm) {
    attrs {
        this.onSubmit = onSubmit
        this.onRemove = onRemoveFunc
    }
    editorDiv(tribe, player, onChange)
}

private fun RBuilder.editorDiv(tribe: Tribe, player: Player, onChange: (Event) -> Unit) = div {
    editor {
        li { nameInput(player, onChange) }
        li { emailInput(player, onChange) }
        if (tribe.callSignsEnabled) {
            callSignConfig(player, onChange)
        }
        if (tribe.badgesEnabled) {
            badgeConfig(tribe, player, onChange, styles["badgeConfig"])
        }
    }
}

private fun RBuilder.nameInput(player: Player, onChange: (Event) -> Unit) {
    configInput(
        labelText = "Name",
        id = "player-name",
        name = "name",
        value = player.name,
        type = InputType.text,
        onChange = onChange,
        placeholder = "My name is..."
    )
    span { +"What's your moniker?" }
}

private fun RBuilder.emailInput(player: Player, onChange: (Event) -> Unit) {
    configInput(
        labelText = "Email",
        id = "player-email",
        name = "email",
        value = player.email,
        type = InputType.text,
        onChange = onChange,
        placeholder = "email"
    )
    span {
        +"Email provides access privileges, so you can see all Tribes you're in!"
        +"To change your player picture, assign a"
        child(gravatarLink)
        +"to this email."
    }
}

private fun RBuilder.callSignConfig(player: Player, onChange: (Event) -> Unit) {
    li {
        configInput(
            labelText = "Call-Sign Adjective",
            id = "adjective-input",
            name = "callSignAdjective",
            value = player.callSignAdjective,
            type = InputType.text,
            onChange = onChange,
            list = "callSignAdjectiveOptions"
        )
        datalist { attrs { id = "callSignAdjectiveOptions" } }
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
            list = "callSignNounOptions"
        )
        datalist { attrs { id = "callSignNounOptions" } }
        span { +"... the need for speed!" }
    }

}

private fun RBuilder.badgeConfig(
    tribe: Tribe,
    player: Player,
    onChange: (Event) -> Unit,
    className: String
) = li(classes = className) {
    label { attrs { htmlFor = "badge" }; +"Badge" }
    select {
        attrs {
            id = "badge"
            name = "badge"
            this["value"] = "${player.badge}"
            onChangeFunction = onChange
        }
        defaultBadgeOption(tribe)
        altBadgeOption(tribe)
    }
    span { +"Your badge makes you feel... different than the others." }
}

private fun RBuilder.altBadgeOption(tribe: Tribe) = option {
    attrs {
        id = "alt-badge-option"
        key = "${Badge.Alternate.value}"
        value = "${Badge.Alternate.value}"
        label = tribe.alternateBadgeName
    }
}

private fun RBuilder.defaultBadgeOption(tribe: Tribe) = option {
    attrs {
        id = "default-badge-option"
        key = "${Badge.Default.value}"
        value = "${Badge.Default.value}"
        label = tribe.defaultBadgeName
    }
}
