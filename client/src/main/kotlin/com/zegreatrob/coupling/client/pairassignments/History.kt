package com.zegreatrob.coupling.client.pairassignments

import com.soywiz.klock.DateFormat
import com.zegreatrob.coupling.client.ReactFunctionComponent
import com.zegreatrob.coupling.client.component
import com.zegreatrob.coupling.client.styledComponent
import com.zegreatrob.coupling.client.tribe.TribeCardProps
import com.zegreatrob.coupling.client.tribe.TribeCardRenderer
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.toJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Window
import react.RBuilder
import react.RProps
import react.dom.div
import react.dom.span
import kotlin.js.Promise

interface HistorySyntax {
    fun RBuilder.history(props: HistoryProps) = component(history, props)

    companion object : HistoryComponentBuilder {
        private val history = build()
    }
}

interface HistoryComponentBuilder : ComponentBuilder<HistoryProps>, WindowFunctions, TribeCardRenderer {

    val scope: CoroutineScope get() = MainScope()

    override fun build() = styledComponent<HistoryProps, HistoryStyles>(
            "pairassignments/History"
    ) { props, styles ->
        val (tribe, pathSetter) = props

        div {
            div {
                attrs { id = "tribe-browser" }
                tribeCard(TribeCardProps(tribe, pathSetter = pathSetter))
            }
            span {
                attrs { id = "history-view" }
                div(classes = "header") { +"History!" }
                pairAssignmentList(props)
            }
        }
    }

    private fun RBuilder.pairAssignmentList(props: HistoryProps) = props.history.map {
        div(classes = "pair-assignments") {
            attrs { key = it.id?.value ?: "" }
            span(classes = "pair-assignments-header") { +it.dateText() }
            span(classes = "small red button delete-button") {
                attrs { onClickFunction = { _ -> scope.launch { props.removeButtonOnClick(it) } } }
                +"DELETE"
            }
            div { showPairs(it) }
        }
    }

    private fun PairAssignmentDocument.dateText() =
            "${date.format(DateFormat("MM/dd/YYYY"))} - ${date.format(DateFormat("HH:mm:ss"))}"

    private suspend fun HistoryProps.removeButtonOnClick(document: PairAssignmentDocument) {
        console.log("REMOVE BUTTON CLICK")
        if (window.confirm("Are you sure you want to delete these pair assignments?")) {
            coupling.removeAssignments(document.toJson(), tribe.id.value)
                    .unsafeCast<Promise<Unit>>()
                    .await()
            reload()
        }
    }

    private fun RBuilder.showPairs(document: PairAssignmentDocument) =
            document.pairs.mapIndexed { index, pair ->
                span(classes = "pair") {
                    attrs { key = "$index" }
                    pair.players.map { pinnedPlayer: PinnedPlayer ->
                        span(classes = "player") {
                            attrs { key = "${pinnedPlayer.player.id}" }
                            div(classes = "player-header") {
                                +(pinnedPlayer.player.name ?: "Unknown")
                            }
                        }
                    }
                }
            }
}

interface ComponentBuilder<P : RProps> {
    fun build(): ReactFunctionComponent<P>
}

interface WindowFunctions {
    val window: Window get() = kotlin.browser.window
}

external interface HistoryStyles

data class HistoryProps(
        val tribe: KtTribe,
        val pathSetter: (String) -> Unit,
        val history: List<PairAssignmentDocument>,
        val coupling: dynamic,
        val reload: () -> Unit
) : RProps
