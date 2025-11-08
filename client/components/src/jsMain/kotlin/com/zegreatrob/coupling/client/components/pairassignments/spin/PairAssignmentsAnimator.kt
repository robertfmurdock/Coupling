package com.zegreatrob.coupling.client.components.pairassignments.spin

import com.zegreatrob.coupling.client.components.FrameRunner
import com.zegreatrob.coupling.client.components.animationsDisabledContext
import com.zegreatrob.coupling.client.components.external.reactfliptoolkit.Flipper
import com.zegreatrob.coupling.client.components.spin.RosteredPairAssignments
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.ChildrenBuilder
import react.Consumer
import react.PropsWithChildren
import react.create
import react.dom.html.ReactHTML.div

private val animationContextConsumer: Consumer<Boolean> = animationsDisabledContext.Consumer

external interface PairAssignmentsAnimatorProps : PropsWithChildren {
    var party: PartyDetails
    var players: List<Player>
    var pairAssignments: PairingSet
    var enabled: Boolean
}

@ReactFunc
val PairAssignmentsAnimator by nfc<PairAssignmentsAnimatorProps> { props ->
    val (party, players, pairAssignments, enabled) = props
    animationContextConsumer {
        children = { animationsDisabled ->
            div.create {
                if (!animationsDisabled && enabled) {
                    spinFrameRunner(pairAssignments, party, players, props)
                } else {
                    +props.children
                }
            }
        }
    }
}

private fun ChildrenBuilder.spinFrameRunner(
    pairAssignments: PairingSet,
    party: PartyDetails,
    players: List<Player>,
    props: PairAssignmentsAnimatorProps,
) {
    FrameRunner(
        sequence = SpinAnimationState.sequence(pairAssignments),
        speed = party.animationSpeed,
        child = { state ->
            Flipper.create {
                flipKey = state.toString()
                if (state == End) {
                    +props.children
                } else {
                    SpinAnimationPanel(
                        party,
                        RosteredPairAssignments.rosteredPairAssignments(pairAssignments, players),
                        state,
                    )
                }
            }
        },
    )
}
