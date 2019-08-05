package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.client.external.reactrouter.prompt
import com.zegreatrob.coupling.client.tribe.TribeCardProps
import com.zegreatrob.coupling.client.tribe.tribeCard
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.toJson
import com.zegreatrob.coupling.common.toPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import kotlinx.html.tabIndex
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.dom.*
import kotlin.js.json

object PlayerConfig : ComponentProvider<PlayerConfigProps>(), PlayerConfigBuilder

data class PlayerConfigProps(
        val tribe: KtTribe,
        val player: Player,
        val players: List<Player>,
        val pathSetter: (String) -> Unit,
        val coupling: Coupling,
        val reload: () -> Unit
) : RProps

external interface PlayerConfigStyles {
    val className: String
    val tribeBrowser: String
    val playerView: String
    val playerRoster: String
}

val playerDefaults get() = json("badge" to Badge.Default.value)

interface PlayerConfigBuilder : ScopedStyledComponentBuilder<PlayerConfigProps, PlayerConfigStyles>,
        WindowFunctions, UseFormHook {

    override val componentPath: String get() = "player/PlayerConfig"

    override fun build() = buildBy {
        val (tribe, _, players, pathSetter) = props

        {
            div(classes = styles.className) {
                div {
                    div(classes = styles.tribeBrowser) {
                        tribeCard(TribeCardProps(tribe, pathSetter = pathSetter))
                    }

                    playerView(this)
                }
                playerRoster(PlayerRosterProps(
                        players = players,
                        tribeId = tribe.id,
                        pathSetter = pathSetter,
                        className = styles.playerRoster
                ))
            }
        }
    }

    private fun ScopedPropsStylesBuilder<PlayerConfigProps, PlayerConfigStyles>.playerView(rBuilder: RBuilder) {
        val (tribe, _, _, pathSetter, coupling, reload) = props
        val player = props.player.withDefaults()

        val (values, onChange) = useForm(player.toJson())
        val updatedPlayer = values.toPlayer()
        val onSubmitFunc = handleSubmitFunc { savePlayer(scope, coupling, updatedPlayer, tribe, reload) }
        val removePlayerFunc = { removePlayer(coupling, player, tribe, pathSetter, scope) }

        val shouldShowPrompt = updatedPlayer != player
        rBuilder.run {
            span(classes = styles.playerView) {
                span(classes = "player") {
                    playerConfigForm(updatedPlayer, tribe, onChange, onSubmitFunc, removePlayerFunc)
                    prompt(
                            `when` = shouldShowPrompt,
                            message = "You have unsaved data. Would you like to save before you leave?"
                    )
                }
                playerCard(PlayerCardProps(tribe.id, updatedPlayer, size = 250, pathSetter = {}))
            }
        }
    }

    private fun Player.withDefaults() = playerDefaults.add(toJson()).toPlayer()

    private fun handleSubmitFunc(handler: () -> Job) = { event: Event ->
        event.preventDefault()
        handler()
    }

    private fun savePlayer(
            scope: CoroutineScope,
            coupling: Coupling,
            updatedPlayer: Player,
            tribe: KtTribe,
            reload: () -> Unit
    ) = scope.launch {
        coupling.savePlayer(updatedPlayer, tribe)
        reload()
    }

    private fun removePlayer(
            coupling: Coupling,
            player: Player,
            tribe: KtTribe,
            pathSetter: (String) -> Unit,
            scope: CoroutineScope
    ) = scope.launch {
        if (window.confirm("Are you sure you want to delete this player?")) {
            coupling.removePlayer(player, tribe)
            pathSetter("/${tribe.id.value}/pairAssignments/current/")
        }
    }

    private fun RBuilder.playerConfigForm(
            player: Player,
            tribe: KtTribe,
            onChange: (Event) -> Unit,
            onSubmit: (Event) -> Job,
            removePlayer: () -> Job
    ) {
        val (isSaving, setIsSaving) = useState(false)

        form {
            attrs { name = "playerForm"; onSubmitFunction = { event -> setIsSaving(true); onSubmit(event) } }

            div {
                configInput(
                        labelText = "Name",
                        id = "player-name",
                        name = "name",
                        value = player.name ?: "",
                        type = InputType.text,
                        onChange = onChange
                )
            }
            div {
                configInput(
                        labelText = "Email",
                        id = "player-email",
                        name = "email",
                        value = player.email ?: "",
                        type = InputType.text,
                        onChange = onChange
                )
            }
            if (tribe.callSignsEnabled) {
                callSignConfig(player, onChange)
            }
            if (tribe.badgesEnabled) {
                badgeConfig(tribe, player, onChange)
            }
            button(classes = "large blue button save-button") {
                attrs {
                    id = "save-player-button"
                    type = ButtonType.submit
                    tabIndex = "0"
                    value = "Save"
                    disabled = isSaving
                }
                +"Save"
            }
            if (player.id != null) {
                div(classes = "small red button delete-button") {
                    attrs { onClickFunction = { removePlayer() } }
                    +"Retire"
                }
            }
        }

    }

    private fun RBuilder.callSignConfig(player: Player, onChange: (Event) -> Unit) {
        div {
            div {
                configInput(
                        labelText = "Call-Sign Adjective",
                        id = "adjective-input",
                        name = "callSignAdjective",
                        value = player.callSignAdjective ?: "",
                        type = InputType.text,
                        onChange = onChange,
                        list = "callSignAdjectiveOptions"
                )
                dataList { attrs { id = "callSignAdjectiveOptions" } }
            }
            div {
                configInput(
                        labelText = "Call-Sign Noun",
                        id = "noun-input",
                        name = "callSignNoun",
                        value = player.callSignNoun ?: "",
                        type = InputType.text,
                        onChange = onChange,
                        list = "callSignNounOptions"
                )
                dataList { attrs { id = "callSignNounOptions" } }
            }
        }
    }

    private fun RBuilder.badgeConfig(tribe: KtTribe, player: Player, onChange: (Event) -> Unit) {
        div(classes = "badge-config") {
            div {
                configInput(
                        labelText = tribe.defaultBadgeName ?: "",
                        id = "default-badge-radio",
                        name = "badge",
                        value = "${Badge.Default.value}",
                        type = InputType.radio,
                        onChange = onChange,
                        checked = player.badge == Badge.Default.value
                )
            }
            div {
                configInput(
                        labelText = tribe.alternateBadgeName ?: "",
                        id = "alt-badge-radio",
                        name = "badge",
                        value = "${Badge.Alternate.value}",
                        type = InputType.radio,
                        onChange = onChange,
                        checked = player.badge == Badge.Alternate.value
                )
            }
        }
    }

}
