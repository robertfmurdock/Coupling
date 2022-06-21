package com.zegreatrob.coupling.client.pairassignments.list

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import com.zegreatrob.coupling.client.ConfigHeader
import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.red
import com.zegreatrob.coupling.client.dom.small
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.react.windowReactFunc
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.pin.PinButtonScale
import com.zegreatrob.coupling.client.pin.PinSection
import com.zegreatrob.coupling.client.pngPath
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import csstype.Auto
import csstype.BackgroundRepeat
import csstype.Border
import csstype.BoxShadow
import csstype.Clear
import csstype.Color
import csstype.Display
import csstype.FontWeight
import csstype.LineStyle
import csstype.Margin
import csstype.NamedColor
import csstype.None
import csstype.Overflow
import csstype.Padding
import csstype.Position
import csstype.TextAlign
import csstype.px
import csstype.rgba
import csstype.url
import csstype.vh
import emotion.css.ClassName
import emotion.react.css
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.key

private val styles = useStyles("pairassignments/History")

data class History(
    val party: Party,
    val history: List<PairAssignmentDocument>,
    val controls: Controls<DeletePairAssignmentsCommandDispatcher>
) : DataPropsBind<History>(com.zegreatrob.coupling.client.pairassignments.list.history)

val history by lazy { historyFunc(WindowFunctions) }

val historyFunc = windowReactFunc<History> { (party, history, controls), windowFuncs ->
    val (dispatchFunc, reload) = controls
    val onDeleteFactory = { documentId: PairAssignmentDocumentId ->
        val deleteFunc = dispatchFunc({ DeletePairAssignmentsCommand(party.id, documentId) }, { reload() })
        onDeleteClick(windowFuncs, deleteFunc)
    }
    div {
        css(styles.className) {
            display = Display.inlineBlock
            backgroundColor = Color("#dae8e0")
            padding = Padding(0.px, 25.px, 25.px, 25.px)
            minHeight = 100.vh
            border = Border(12.px, LineStyle.solid, Color("#4f5853"))
            borderTop = 2.px
            borderBottom = 2.px
            borderRadius = 82.px
        }
        ConfigHeader {
            this.party = party
            +"History!"
        }
        span {
            css(styles["historyView"]) {
                display = Display.inlineBlock
            }
            history.forEach {
                pairAssignmentRow(it, onDeleteFactory(it.id))
            }
        }
    }
}

private fun onDeleteClick(windowFunctions: WindowFunctions, deleteFunc: () -> Unit) = fun() {
    if (windowFunctions.window.confirm("Are you sure you want to delete these pair assignments?")) {
        deleteFunc.invoke()
    }
}

private fun ChildrenBuilder.pairAssignmentRow(document: PairAssignmentDocument, onDeleteClick: () -> Unit) = div {
    css(styles["pairAssignments"]) {
        borderRadius = 20.px
        padding = 5.px
        margin = Margin(5.px, 0.px)
        backgroundColor = Color("#C3D5CBFF")
        boxShadow = BoxShadow(1.px, 1.px, 3.px, rgba(0, 0, 0, 0.6))
    }
    key = document.id.value
    span {
        css {
            display = Display.inlineBlock
            fontSize = 28.px
            fontWeight = FontWeight.bold
            borderRadius = 15.px
            paddingLeft = 40.px
            paddingRight = 5.px
            paddingBottom = 6.px
        }
        +document.dateText()
    }
    deleteButton(onClickFunc = onDeleteClick)
    showPairs(document)
}

private fun ChildrenBuilder.deleteButton(onClickFunc: () -> Unit) = add(
    CouplingButton(
        sizeRuleSet = small,
        colorRuleSet = red,
        className = styles["deleteButton"],
        onClick = onClickFunc
    )
) {
    +"DELETE"
}

private fun ChildrenBuilder.showPairs(document: PairAssignmentDocument) = div {
    document.pairs.mapIndexed { index, pair ->
        span {
            css {
                border = Border(3.px, LineStyle.double, NamedColor.dimgray)
                backgroundColor = NamedColor.aliceblue
                padding = 5.px
                display = Display.inlineBlock
                borderRadius = 40.px
                margin = Margin(0.px, 2.px, 0.px, 2.px)
                position = Position.relative
            }
            key = "$index"
            pair.players.map { pinnedPlayer: PinnedPlayer ->
                showPlayer(pinnedPlayer)
            }
            add(
                PinSection(
                    pinList = pair.pins.toList(),
                    scale = PinButtonScale.ExtraSmall,
                    className = ClassName {
                        bottom = 2.px
                    }
                )
            )
        }
    }
}

private fun ChildrenBuilder.showPlayer(pinnedPlayer: PinnedPlayer) = span {
    css {
        width = Auto.auto
        height = Auto.auto
        position = Position.relative
        clear = Clear.both
        display = Display.inlineBlock
        overflow = Overflow.hidden
        border = Border(3.px, LineStyle.outset, NamedColor.gold)
        backgroundColor = NamedColor.darkseagreen
        backgroundImage = url(pngPath("overlay"))
        backgroundRepeat = BackgroundRepeat.repeatX
        padding = 6.px
        textAlign = TextAlign.center
        textDecoration = None.none
        borderRadius = 6.px
        boxShadow = BoxShadow(0.px, 1.px, 3.px, rgba(0, 0, 0, 0.6))
        color = NamedColor.black
        margin = Margin(0.px, 2.px, 0.px, 2.px)
    }
    key = pinnedPlayer.player.id
    div {
        css {
            backgroundColor = NamedColor.darkcyan
            backgroundImage = url(pngPath("overlay"))
            backgroundRepeat = BackgroundRepeat.repeatX
            asDynamic()["margin-before"] = "6px"
            asDynamic()["margin-after"] = "6px"
            borderRadius = 15.px
            fontWeight = FontWeight.bold
        }
        +pinnedPlayer.player.name
    }
}

fun PairAssignmentDocument.dateText() = date.local.dateText()

private fun DateTimeTz.dateText() = "${format(DateFormat("MM/dd/YYYY"))} - ${format(DateFormat("HH:mm:ss"))}"
