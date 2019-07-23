package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.client.external.reactrouter.prompt
import com.zegreatrob.coupling.client.tribe.TribeCardProps
import com.zegreatrob.coupling.client.tribe.tribeCard
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.toJson
import com.zegreatrob.coupling.common.toPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import kotlinx.html.tabIndex
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.dom.*
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json

object PlayerConfig : ComponentProvider<PlayerConfigProps>(), PlayerConfigBuilder

val RBuilder.playerConfig get() = PlayerConfig.captor(this)

data class PlayerConfigProps(
        val tribe: KtTribe,
        val player: Player,
        val players: List<Player>,
        val pathSetter: (String) -> Unit,
        val coupling: dynamic,
        val reload: () -> Unit
) : RProps

external interface PlayerConfigStyles

val playerDefaults get() = json("badge" to Badge.Default.value)

interface PlayerConfigBuilder : ScopedStyledComponentBuilder<PlayerConfigProps, PlayerConfigStyles>,
        PlayerRosterRenderer, WindowFunctions {

    override val componentPath: String get() = "player/PlayerConfig"

    override fun build() = buildBy {
        val (tribe, _, players, pathSetter, coupling, reload) = props
        val player = playerDefaults.add(props.player.toJson()).toPlayer()

        val (values, handleChange) = useForm(
                player.toJson()
        )
        val updatedPlayer = values.toPlayer()
        val handleSubmit = handleSubmitFunc(coupling, updatedPlayer, tribe, reload)
        val removePlayerFunc = removePlayerFunc(coupling, player, tribe, pathSetter)

        val shouldShowPrompt = updatedPlayer != player

        {
            div(classes = "react-player-config") {
                div {
                    div {
                        attrs { id = "tribe-browser" }
                        tribeCard(TribeCardProps(tribe, pathSetter = pathSetter))
                    }
                    span {
                        attrs { id = "player-view" }
                        span(classes = "player") {
                            playerConfigForm(updatedPlayer, tribe, handleChange, handleSubmit, removePlayerFunc)
                            prompt(
                                    `when` = shouldShowPrompt,
                                    message = "You have unsaved data. Would you like to save before you leave?"
                            )
                        }
                        playerCard(PlayerCardProps(tribe.id, updatedPlayer, size = 250, pathSetter = {}))
                    }
                }
                playerRoster(PlayerRosterProps(
                        players = players,
                        tribeId = tribe.id,
                        pathSetter = pathSetter,
                        className = "player-roster"
                ))
            }
        }
    }

    private fun ScopedPropsStylesBuilder<PlayerConfigProps, PlayerConfigStyles>.handleSubmitFunc(
            coupling: dynamic,
            updatedPlayer: Player,
            tribe: KtTribe,
            reload: () -> Unit
    ): (Event) -> Job {
        fun savePlayer() = scope.launch {
            coupling.savePlayer(updatedPlayer.toJson(), tribe.id.value)
                    .unsafeCast<Promise<Unit>>()
                    .await()
            reload()
        }

        return { event: Event ->
            event.preventDefault()
            savePlayer()
        }
    }

    private fun ScopedPropsStylesBuilder<PlayerConfigProps, PlayerConfigStyles>.removePlayerFunc(
            coupling: dynamic,
            player: Player,
            tribe: KtTribe,
            pathSetter: (String) -> Unit
    ): () -> Job = {
        scope.launch {
            if (window.confirm("Are you sure you want to delete this player?")) {
                coupling.removePlayer(player.toJson(), tribe.id.value)
                        .unsafeCast<Promise<Unit>>()
                        .await()
                pathSetter("/${tribe.id.value}/pairAssignments/current/")
            }
        }
    }

    fun useForm(initialValues: Json): Pair<Json, (Event) -> Unit> {
        val (values, setValues) = useStateWithSetterFunction(initialValues)

        val handleChange = { event: Event ->
            event.unsafeCast<dynamic>().persist()
            setValues { previousValues ->
                val target = event.target.unsafeCast<Json>()
                val name = target["name"].unsafeCast<String>()
                val value = target["value"].unsafeCast<String>()

                json()
                        .add(previousValues)
                        .add(json(name to value))
            }
        }

        return Pair(
                values,
                handleChange
        )
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
                label { attrs { htmlFor = "player-name" }; +"Name" }
                input {
                    attrs {
                        name = "name"
                        id = "player-name"
                        type = InputType.text
                        value = player.name ?: ""
                        onChangeFunction = onChange
                    }
                }
            }
            div {
                label { attrs { htmlFor = "player-email" }; +"Email" }
                input {
                    attrs {
                        name = "email"
                        id = "player-email"
                        type = InputType.text
                        value = player.email ?: ""
                        onChangeFunction = onChange
                    }
                }
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
                label { attrs { htmlFor = "adjective-input" }; +"Call-Sign Adjective" }
                input {
                    attrs {
                        name = "callSignAdjective"
                        id = "adjective-input"
                        type = InputType.text
                        list = "callSignAdjectiveOptions"
                        value = player.callSignAdjective ?: ""
                        onChangeFunction = onChange
                    }
                }
                dataList { attrs { id = "callSignAdjectiveOptions" } }
            }
            div {
                label { attrs { htmlFor = "noun-input" }; +"Call-Sign Noun" }
                input {
                    attrs {
                        name = "callSignNoun"
                        id = "noun-input"
                        type = InputType.text
                        list = "callSignNounOptions"
                        value = player.callSignNoun ?: ""
                        onChangeFunction = onChange
                    }
                }
                dataList { attrs { id = "callSignNounOptions" } }
            }
        }
    }

    private fun RBuilder.badgeConfig(tribe: KtTribe, player: Player, onChange: (Event) -> Unit) {
        div(classes = "badge-config") {
            div {
                label { attrs { htmlFor = "default-badge-radio" }; +(tribe.defaultBadgeName ?: "") }
                input {
                    attrs {
                        name = "badge"
                        id = "default-badge-radio"
                        type = InputType.radio
                        value = "${Badge.Default.value}"
                        checked = player.badge == Badge.Default.value
                        onChangeFunction = onChange
                    }
                }
            }
            div {
                label { attrs { htmlFor = "alt-badge-radio" }; +(tribe.alternateBadgeName ?: "") }
                input {
                    attrs {
                        name = "badge"
                        id = "alt-badge-radio"
                        type = InputType.radio
                        value = "${Badge.Alternate.value}"
                        checked = player.badge == Badge.Alternate.value
                        onChangeFunction = onChange
                    }
                }
            }
        }
    }

}
