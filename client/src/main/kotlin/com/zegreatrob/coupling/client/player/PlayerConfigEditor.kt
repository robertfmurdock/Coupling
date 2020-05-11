package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.configHeader
import com.zegreatrob.coupling.client.editor
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.reactrouter.prompt
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.invoke
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.html.*
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.ReactElement
import react.dom.*

data class PlayerConfigEditorProps(
    val tribe: Tribe,
    val player: Player,
    val pathSetter: (String) -> Unit,
    val reload: () -> Unit,
    val dispatchFunc: DispatchFunc<out PlayerConfigDispatcher>
) : RProps

fun RBuilder.playerConfigEditor(
    tribe: Tribe,
    player: Player,
    pathSetter: (String) -> Unit,
    reload: () -> Unit,
    dispatchFunc: DispatchFunc<out PlayerConfigDispatcher>
) = child(
    PlayerConfigEditor.component.rFunction,
    PlayerConfigEditorProps(tribe, player, pathSetter, reload, dispatchFunc)
)

private val styles = useStyles("player/PlayerConfigEditor")

val PlayerConfigEditorComponent = windowReactFunc<PlayerConfigEditorProps> { props, windowFuncs ->
    val (tribe, player, pathSetter, reload, commandFunc) = props
    val (values, onChange) = useForm(player.toJson())

    val updatedPlayer = values.toPlayer()
    val onSubmitFunc = preventDefault(commandFunc({ SavePlayerCommand(tribe.id, updatedPlayer) }, { reload() }))
    val onRemoveFunc = { playerId: String -> removePlayer(tribe, playerId, pathSetter, windowFuncs, commandFunc) }
    span(classes = styles.className) {
        configHeader(tribe, pathSetter) { +"Player Configuration" }
        div {
            div(classes = styles["player"]) {
                playerConfigForm(updatedPlayer, tribe, onChange, onSubmitFunc, onRemoveFunc)
                promptOnExit(shouldShowPrompt = updatedPlayer != player)
            }
            playerCard(PlayerCardProps(tribe.id, updatedPlayer, size = 250, pathSetter = {}))
        }
    }
}

val PlayerConfigEditor by lazy { PlayerConfigEditorComponent(WindowFunctions) }

private fun removePlayer(
    tribe: Tribe,
    playerId: String,
    pathSetter: (String) -> Unit,
    windowFunctions: WindowFunctions,
    dispatchFunc: DispatchFunc<out DeletePlayerCommandDispatcher>
): () -> Unit = {
    if (windowFunctions.window.confirm("Are you sure you want to delete this player?")) {
        dispatchFunc({ DeletePlayerCommand(tribe.id, playerId) }, { pathSetter(tribe.id.currentPairsPage()) })
            .invoke()
    }
}

private fun TribeId.currentPairsPage() = "/$value/pairAssignments/current/"

private fun RBuilder.promptOnExit(shouldShowPrompt: Boolean) = prompt(
    `when` = shouldShowPrompt,
    message = "You have unsaved data. Would you like to save before you leave?"
)

private inline fun preventDefault(crossinline handler: () -> Unit) = { event: Event ->
    event.preventDefault()
    handler()
}

private inline fun RBuilder.playerConfigForm(
    player: Player,
    tribe: Tribe,
    noinline onChange: (Event) -> Unit,
    crossinline onSubmit: (Event) -> Unit,
    crossinline removePlayerFunc: (String) -> () -> Unit
): ReactElement {
    val (isSaving, setIsSaving) = useState(false)
    return form {
        attrs {
            name = "playerForm"
            onSubmitFunction = { event -> setIsSaving(true); onSubmit(event) }
        }
        div {
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
        saveButton(isSaving, styles["saveButton"])
        val playerId = player.id
        if (playerId != null) {
            retireButton(removePlayerFunc(playerId))
        }
    }
}

private fun RBuilder.retireButton(onRetire: () -> Unit) = div(classes = "small red button") {
    attrs {
        classes += styles["deleteButton"]
        onClickFunction = { onRetire() }
    }
    +"Retire"
}

private fun RBuilder.saveButton(isSaving: Boolean, className: String) = button(classes = "super blue button") {
    attrs {
        classes += className
        type = ButtonType.submit
        tabIndex = "0"
        value = "Save"
        disabled = isSaving
    }
    +"Save"
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
    span { +"Email provides access privileges, so you can see all Tribes you're in!" }
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
        dataList { attrs { id = "callSignAdjectiveOptions" } }
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
        dataList { attrs { id = "callSignNounOptions" } }
        span { +"... the need for speed!" }
    }

}

private fun RBuilder.badgeConfig(
    tribe: Tribe,
    player: Player,
    onChange: (Event) -> Unit,
    className: String
) {
    li(classes = className) {
        label { attrs { htmlFor = "badge" }; +"Badge" }
        select {
            attrs {
                id = "badge"
                name = "badge"
                this["value"] = "${player.badge}"
                onChangeFunction = onChange
            }
            option {
                attrs {
                    id = "default-badge-option"
                    key = "${Badge.Default.value}"
                    value = "${Badge.Default.value}"
                    label = tribe.defaultBadgeName
                }
            }
            option {
                attrs {
                    id = "alt-badge-option"
                    key = "${Badge.Alternate.value}"
                    value = "${Badge.Alternate.value}"
                    label = tribe.alternateBadgeName
                }
            }
        }
        span { +"Your badge makes you feel... different than the others." }
    }
}