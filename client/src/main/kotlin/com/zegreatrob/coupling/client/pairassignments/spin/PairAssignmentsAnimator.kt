package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.FrameRunner
import com.zegreatrob.coupling.client.animationsDisabledContext
import com.zegreatrob.coupling.client.external.reactfliptoolkit.Flipper
import com.zegreatrob.coupling.client.pairassignments.spin.RosteredPairAssignments.Companion.rosteredPairAssignments
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import react.ChildrenBuilder
import react.Consumer
import react.create
import react.dom.html.ReactHTML.div

data class PairAssignmentsAnimator(
    val party: Party,
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument,
    val enabled: Boolean,
    val children: ChildrenBuilder.() -> Unit
) : DataPropsBind<PairAssignmentsAnimator>(pairAssignmentsAnimator)

private val animationContextConsumer: Consumer<Boolean> = animationsDisabledContext.Consumer

val pairAssignmentsAnimator = tmFC<PairAssignmentsAnimator> { props ->
    val (party, players, pairAssignments, enabled) = props
    animationContextConsumer {
        children = { animationsDisabled ->
            div.create {
                if (!animationsDisabled && enabled) {
                    spinFrameRunner(pairAssignments, party, players, props)
                } else {
                    props.children(this)
                }
            }
        }
    }
}

private fun ChildrenBuilder.spinFrameRunner(
    pairAssignments: PairAssignmentDocument,
    party: Party,
    players: List<Player>,
    props: PairAssignmentsAnimator
) {
    add(
        FrameRunner(SpinAnimationState.sequence(pairAssignments), speed = party.animationSpeed) { state ->
            val rosteredPairAssignments = rosteredPairAssignments(pairAssignments, players)
            flipperSpinAnimation(state, props, party, rosteredPairAssignments)
        }
    )
}

private fun ChildrenBuilder.flipperSpinAnimation(
    state: SpinAnimationState,
    props: PairAssignmentsAnimator,
    party: Party,
    rosteredPairAssignments: RosteredPairAssignments
) = Flipper {
    flipKey = state.toString()
    if (state == End)
        props.children(this)
    else
        add(SpinAnimationPanel(party, rosteredPairAssignments, state))
}
