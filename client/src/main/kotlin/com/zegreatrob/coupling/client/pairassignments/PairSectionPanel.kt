package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.spin.PairAssignmentsAnimator
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.Border
import csstype.BoxShadow
import csstype.Display
import csstype.FontSize
import csstype.FontWeight
import csstype.LineStyle
import csstype.Margin
import csstype.NamedColor
import csstype.Padding
import csstype.PropertiesBuilder
import csstype.px
import csstype.rgb
import csstype.rgba
import emotion.react.css
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div

data class PairSectionPanel(
    val party: Party,
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument?,
    val allowSave: Boolean,
    val setPairs: (PairAssignmentDocument) -> Unit,
    val controls: Controls<DeletePairAssignmentsCommandDispatcher>
) : DataPropsBind<PairSectionPanel>(pairSectionPanel)

val pairSectionPanel = tmFC<PairSectionPanel> { (party, players, pairAssignments, allowSave, setPairs, controls) ->
    div {
        css { pairSectionCss() }
        if (pairAssignments == null) {
            noPairsHeader()
        } else {
            add(
                PairAssignmentsAnimator(
                    party = party,
                    players = players,
                    pairAssignments = pairAssignments,
                    enabled = party.animationEnabled && allowSave
                ) {
                    add(
                        CurrentPairAssignmentsPanel(
                            party = party,
                            pairAssignments = pairAssignments,
                            setPairAssignments = setPairs,
                            allowSave = allowSave,
                            dispatchFunc = controls.dispatchFunc
                        )
                    )
                }
            )
        }
    }
}

private fun PropertiesBuilder.pairSectionCss() {
    display = Display.inlineBlock
    borderRadius = 20.px
    padding = 5.px
    margin = Margin(5.px, 0.px)
    backgroundColor = rgb(195, 213, 203)
    boxShadow = BoxShadow(1.px, 1.px, 3.px, rgba(0, 0, 0, 0.6))
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
