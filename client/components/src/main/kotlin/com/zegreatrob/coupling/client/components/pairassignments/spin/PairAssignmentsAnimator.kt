package com.zegreatrob.coupling.client.components.pairassignments.spin

import com.zegreatrob.coupling.client.components.FrameRunner
import com.zegreatrob.coupling.client.components.animationsDisabledContext
import com.zegreatrob.coupling.client.components.external.reactfliptoolkit.Flipper
import com.zegreatrob.coupling.client.components.spin.RosteredPairAssignments
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.ntmFC
import react.ChildrenBuilder
import react.Consumer
import react.create
import react.dom.html.ReactHTML

data class PairAssignmentsAnimator(
    val party: Party,
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument,
    val enabled: Boolean,
    val children: ChildrenBuilder.() -> Unit,
) : DataPropsBind<PairAssignmentsAnimator>(pairAssignmentsAnimator)

private val animationContextConsumer: Consumer<Boolean> = animationsDisabledContext.Consumer
val pairAssignmentsAnimator by ntmFC<PairAssignmentsAnimator> { props ->
    val (party, players, pairAssignments, enabled) = props
    animationContextConsumer {
        children = { animationsDisabled ->
            ReactHTML.div.create {
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
    props: PairAssignmentsAnimator,
) {
    add(
        FrameRunner(
            SpinAnimationState.sequence(
                pairAssignments,
            ),
            speed = party.animationSpeed,
        ) { state ->
            val rosteredPairAssignments = RosteredPairAssignments.rosteredPairAssignments(pairAssignments, players)
            flipperSpinAnimation(state, props, party, rosteredPairAssignments)
        },
    )
}

private fun ChildrenBuilder.flipperSpinAnimation(
    state: SpinAnimationState,
    props: PairAssignmentsAnimator,
    party: Party,
    rosteredPairAssignments: RosteredPairAssignments,
) = Flipper {
    flipKey = state.toString()
    if (state == End) {
        props.children(this)
    } else {
        add(SpinAnimationPanel(party, rosteredPairAssignments, state))
    }
}
