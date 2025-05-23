package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.fire
import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.components.pin.PinButtonScale
import com.zegreatrob.coupling.client.components.pin.PinSection
import com.zegreatrob.coupling.client.components.pngPath
import com.zegreatrob.coupling.client.components.red
import com.zegreatrob.coupling.client.components.small
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.css.ClassName
import emotion.react.css
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import react.ChildrenBuilder
import react.Props
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
import web.cssom.rgb
import web.cssom.url

external interface PairAssignmentRowProps : Props {
    var party: PartyDetails
    var document: PairAssignmentDocument
    var controls: Controls<DeletePairAssignmentsCommand.Dispatcher>
    var windowFunctions: WindowFunctions?
}

@ReactFunc
val PairAssignmentRow by nfc<PairAssignmentRowProps> { props ->
    val (party, document, controls) = props
    val windowFuncs = props.windowFunctions ?: WindowFunctions
    val (dispatchFunc, reload) = controls
    val onDeleteClick: () -> Unit = useCallback(party.id, document.id) {
        val deleteFunc = dispatchFunc {
            fire(DeletePairAssignmentsCommand(party.id, document.id))
            reload()
        }
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
            boxShadow = BoxShadow(1.px, 1.px, 3.px, rgb(0, 0, 0, 0.6))
        }
        asDynamic()["data-pair-assignments-id"] = document.id.value.toString()
        key = document.id.value.toString()
        PairAssignmentBlock {
            +document.dateText()
        }
        deleteButton(onClickFunc = onDeleteClick)
        showPairs(document)
    }
}

private fun ChildrenBuilder.deleteButton(onClickFunc: () -> Unit) = CouplingButton {
    sizeRuleSet = small
    colorRuleSet = red
    onClick = onClickFunc
    +"DELETE"
}

private fun ChildrenBuilder.showPairs(document: PairAssignmentDocument) = div {
    document.pairs.toList().mapIndexed { index, pair ->
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
            pair.pinnedPlayers.toList().map(::showPlayer)
            PinSection(
                pinList = pair.pins.toList(),
                scale = PinButtonScale.ExtraSmall,
                className = ClassName {
                    bottom = 2.px
                },
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
        boxShadow = BoxShadow(0.px, 1.px, 3.px, rgb(0, 0, 0, 0.6))
        color = NamedColor.black
        margin = Margin(0.px, 2.px, 0.px, 2.px)
    }
    key = pinnedPlayer.player.id.value.toString()
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

fun PairAssignmentDocument.dateText() = date.toLocalDateTime(TimeZone.currentSystemDefault()).dateText()

private fun LocalDateTime.dateText() = "$date - $time"
