package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.Editor.editor
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.reactrouter.prompt
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.tribe.TribeCardProps
import com.zegreatrob.coupling.client.tribe.tribeCard
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.sdk.PlayerRepository
import com.zegreatrob.coupling.sdk.RepositoryCatalog
import com.zegreatrob.coupling.sdk.SdkSingleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.ReactElement
import react.dom.*

object PlayerConfig : RComponent<PlayerConfigProps>(provider()), PlayerConfigRenderer,
    RepositoryCatalog by SdkSingleton

data class PlayerConfigProps(
    val tribe: Tribe,
    val player: Player,
    val players: List<Player>,
    val pathSetter: (String) -> Unit,
    val reload: () -> Unit
) : RProps

external interface PlayerConfigStyles {
    val className: String
    val saveButton: String
    val tribeBrowser: String
    val playerView: String
    val playerRoster: String
    val player: String
    val deleteButton: String
    val badgeConfig: String
}

typealias PlayerConfigContext = ScopedStyledRContext<PlayerConfigProps, PlayerConfigStyles>

interface PlayerConfigRenderer : ScopedStyledComponentRenderer<PlayerConfigProps, PlayerConfigStyles>,
    WindowFunctions, UseFormHook, SavePlayerCommandDispatcher, DeletePlayerCommandDispatcher {

    override val playerRepository: PlayerRepository

    override val componentPath: String get() = "player/PlayerConfig"

    override fun ScopedStyledRContext<PlayerConfigProps, PlayerConfigStyles>.render() = with(props) {
        reactElement {
            div(classes = styles.className) {
                div {
                    div(classes = styles.tribeBrowser) {
                        tribeCard(TribeCardProps(tribe, pathSetter = pathSetter))
                    }
                    playerView(this)
                }
                playerRoster(
                    PlayerRosterProps(
                        players = players,
                        tribeId = tribe.id,
                        pathSetter = pathSetter,
                        className = styles.playerRoster
                    )
                )
            }
        }
    }

    private fun PlayerConfigContext.playerView(rBuilder: RBuilder) {
        val (tribe, _, _, _, reload) = props
        val player = props.player

        val (values, onChange) = useForm(player.toJson())
        val updatedPlayer = values.toPlayer()
        val onSubmitFunc = handleSubmitFunc { savePlayer(scope, updatedPlayer, tribe, reload) }

        val shouldShowPrompt = updatedPlayer != player
        rBuilder.run {
            span(classes = styles.playerView) {
                span(classes = styles.player) {
                    div { h1 { +"Player Configuration" } }
                    playerConfigForm(updatedPlayer, tribe, onChange, onSubmitFunc)()
                    prompt(
                        `when` = shouldShowPrompt,
                        message = "You have unsaved data. Would you like to save before you leave?"
                    )
                }
                playerCard(PlayerCardProps(tribe.id, updatedPlayer, size = 250, pathSetter = {}))
            }
        }
    }

    private fun handleSubmitFunc(handler: () -> Job) = { event: Event ->
        event.preventDefault()
        handler()
    }

    private fun savePlayer(
        scope: CoroutineScope,
        updatedPlayer: Player,
        tribe: Tribe,
        reload: () -> Unit
    ) = scope.launch {
        SavePlayerCommand(tribe.id, updatedPlayer).perform()
        reload()
    }

    private fun removePlayer(
        tribe: Tribe,
        pathSetter: (String) -> Unit,
        scope: CoroutineScope,
        playerId: String
    ) = scope.launch {
        if (window.confirm("Are you sure you want to delete this player?")) {
            DeletePlayerCommand(tribe.id, playerId).perform()
            pathSetter("/${tribe.id.value}/pairAssignments/current/")
        }
    }

    private fun PlayerConfigContext.playerConfigForm(
        player: Player,
        tribe: Tribe,
        onChange: (Event) -> Unit,
        onSubmit: (Event) -> Job
    ): RBuilder.() -> ReactElement {
        val (isSaving, setIsSaving) = useState(false)
        return {
            form {
                attrs { name = "playerForm"; onSubmitFunction = { event -> setIsSaving(true); onSubmit(event) } }

                div {
                    editor {
                        li { nameInput(player, onChange) }
                        li { emailInput(player, onChange) }
                        if (tribe.callSignsEnabled) {
                            callSignConfig(player, onChange)
                        }
                        if (tribe.badgesEnabled) {
                            badgeConfig(tribe, player, onChange, styles.badgeConfig)
                        }
                    }
                }
                saveButton(isSaving, styles.saveButton)
                val playerId = player.id
                if (playerId != null) {
                    retireButton(this@playerConfigForm, tribe, playerId)
                }
            }
        }
    }

    private fun RBuilder.retireButton(context: PlayerConfigContext, tribe: Tribe, playerId: String) =
        div(classes = "small red button") {
            attrs {
                classes += context.styles.deleteButton
                onClickFunction = {
                    removePlayer(tribe, context.props.pathSetter, context.scope, playerId)
                }
            }
            +"Retire"
        }

    private fun RBuilder.saveButton(isSaving: Boolean, className: String) = button(classes = "large blue button") {
        attrs {
            classes += className
            type = ButtonType.submit
            tabIndex = "0"
            value = "Save"
            disabled = isSaving
        }
        +"Save"
    }

    private inline fun RBuilder.nameInput(player: Player, noinline onChange: (Event) -> Unit) {
        configInput(
            labelText = "Name",
            id = "player-name",
            name = "name",
            value = player.name ?: "",
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
            value = player.email ?: "",
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
                value = player.callSignAdjective ?: "",
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
                value = player.callSignNoun ?: "",
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
                    this["value"] = "${player.badge ?: Badge.Default.value}"
                    onChangeFunction = onChange
                }
                option {
                    attrs {
                        id = "default-badge-option"
                        key = "${Badge.Default.value}"
                        value = "${Badge.Default.value}"
                        label = tribe.defaultBadgeName ?: ""
                    }
                }
                option {
                    attrs {
                        id = "alt-badge-option"
                        key = "${Badge.Alternate.value}"
                        value = "${Badge.Alternate.value}"
                        label = tribe.alternateBadgeName ?: ""
                    }
                }
            }
            span { +"Your badge makes you feel... different than the others." }
        }
    }

}
