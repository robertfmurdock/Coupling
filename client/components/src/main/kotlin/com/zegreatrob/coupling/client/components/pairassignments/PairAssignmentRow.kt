package com.zegreatrob.coupling.client.components.pairassignments

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.components.PairAssignmentBlock
import com.zegreatrob.coupling.client.components.PinButtonScale
import com.zegreatrob.coupling.client.components.pin.PinSection
import com.zegreatrob.coupling.client.components.pngPath
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.ntmFC
import emotion.css.ClassName
import emotion.react.css
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.useCallback
import web.cssom.Auto
import web.cssom.BackgroundRepeat
import web.cssom.Border
import web.cssom.BoxShadow
import web.cssom.Clear
import web.cssom.Color
import web.cssom.Display
import web.cssom.FontWeight
import web.cssom.LineStyle
import web.cssom.Margin
import web.cssom.NamedColor
import web.cssom.None
import web.cssom.Overflow
import web.cssom.Position
import web.cssom.TextAlign
import web.cssom.px
import web.cssom.rgba
import web.cssom.url

data class PairAssignmentRow(
    val party: Party,
    val document: PairAssignmentDocument,
    val controls: Controls<DeletePairAssignmentsCommand.Dispatcher>,
    val windowFunctions: WindowFunctions = WindowFunctions,

) :
    DataPropsBind<PairAssignmentRow>(pairAssignmentRow)

private val pairAssignmentRow by ntmFC<PairAssignmentRow> { (party, document, controls, windowFuncs) ->
    val (dispatchFunc, reload) = controls
    val onDeleteClick: () -> Unit = useCallback {
        val deleteFunc = dispatchFunc({ DeletePairAssignmentsCommand(party.id, document.id) }, { reload() })
        if (windowFuncs.window.confirm("Are you sure you want to delete these pair assignments?")) {
            deleteFunc.invoke()
        }
    }

    div {
        css {
            borderRadius = 20.px
            padding = 5.px
            margin = Margin(5.px, 0.px)
            backgroundColor = Color("#C3D5CBFF")
            boxShadow = BoxShadow(1.px, 1.px, 3.px, rgba(0, 0, 0, 0.6))
        }
        asDynamic()["data-pair-assignments-id"] = document.id.value
        key = document.id.value
        PairAssignmentBlock {
            +document.dateText()
        }
        deleteButton(onClickFunc = onDeleteClick)
        showPairs(document)
    }
}

private fun ChildrenBuilder.deleteButton(onClickFunc: () -> Unit) = add(
    com.zegreatrob.coupling.client.components.CouplingButton(
        sizeRuleSet = com.zegreatrob.coupling.client.components.small,
        colorRuleSet = com.zegreatrob.coupling.client.components.red,
        onClick = onClickFunc,
    ),
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
                    },
                ),
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
