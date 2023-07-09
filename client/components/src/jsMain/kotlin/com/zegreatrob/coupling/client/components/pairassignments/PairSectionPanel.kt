package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.components.pairassignments.spin.PairAssignmentsAnimator
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import csstype.PropertiesBuilder
import emotion.react.css
import react.ChildrenBuilder
import react.Props
import react.dom.html.ReactHTML.div
import web.cssom.Border
import web.cssom.BoxShadow
import web.cssom.Display
import web.cssom.FontSize
import web.cssom.FontWeight
import web.cssom.LineStyle
import web.cssom.Margin
import web.cssom.NamedColor
import web.cssom.Padding
import web.cssom.px
import web.cssom.rgb

external interface PairSectionPanelProps : Props {
    var party: PartyDetails
    var players: List<Player>
    var pairAssignments: PairAssignmentDocument?
    var allowSave: Boolean
    var setPairs: (PairAssignmentDocument) -> Unit
    var controls: Controls<DeletePairAssignmentsCommand.Dispatcher>
}

@ReactFunc
val PairSectionPanel by nfc<PairSectionPanelProps> { props ->
    val (party, players, pairAssignments, allowSave, setPairs, controls) = props
    div {
        css { pairSectionCss() }
        if (pairAssignments == null) {
            noPairsHeader()
        } else {
            PairAssignmentsAnimator(
                party = party,
                players = players,
                pairAssignments = pairAssignments,
                enabled = party.animationEnabled && allowSave,
            ) {
                CurrentPairAssignmentsPanel(
                    party = party,
                    pairAssignments = pairAssignments,
                    setPairAssignments = setPairs,
                    allowSave = allowSave,
                    dispatchFunc = controls.dispatchFunc,
                )
            }
        }
    }
}

private fun PropertiesBuilder.pairSectionCss() {
    display = Display.inlineBlock
    borderRadius = 20.px
    padding = 5.px
    margin = Margin(5.px, 0.px)
    backgroundColor = rgb(195, 213, 203)
    boxShadow = BoxShadow(1.px, 1.px, 3.px, rgb(0, 0, 0, 0.6))
}

private fun ChildrenBuilder.noPairsHeader() = div {
    css {
        border = Border(8.px, LineStyle.outset, NamedColor.dimgray)
        backgroundColor = NamedColor.aliceblue
        display = Display.inlineBlock
        borderRadius = 40.px
        fontSize = FontSize.xxLarge
        fontWeight = FontWeight.bold
        width = 500.px
        height = 150.px
        padding = Padding(100.px, 5.px, 5.px)
        margin = Margin(0.px, 2.px, 5.px)
    }
    +"No pair assignments yet!"
}
