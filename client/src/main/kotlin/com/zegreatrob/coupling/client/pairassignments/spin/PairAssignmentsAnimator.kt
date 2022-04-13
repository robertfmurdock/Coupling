package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.FrameRunner
import com.zegreatrob.coupling.client.animationsDisabledContext
import com.zegreatrob.coupling.client.external.reactfliptoolkit.Flipper
import com.zegreatrob.coupling.client.pairassignments.spin.RosteredPairAssignments.Companion.rosteredPairAssignments
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import react.ChildrenBuilder
import react.Consumer
import react.create
import react.dom.html.ReactHTML.div

data class PairAssignmentsAnimator(
    val tribe: Party,
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument,
    val enabled: Boolean,
    val children: ChildrenBuilder.() -> Unit
) : DataPropsBind<PairAssignmentsAnimator>(pairAssignmentsAnimator)

private val animationContextConsumer: Consumer<Boolean> = animationsDisabledContext.Consumer

val pairAssignmentsAnimator = tmFC<PairAssignmentsAnimator> { props ->
    val (tribe, players, pairAssignments, enabled) = props
    animationContextConsumer {
        children = { animationsDisabled ->
            div.create {
                if (!animationsDisabled && enabled) {
                    spinFrameRunner(pairAssignments, tribe, players, props)
                } else {
                    props.children(this)
                }
            }
        }
    }
}

private fun ChildrenBuilder.spinFrameRunner(
    pairAssignments: PairAssignmentDocument,
    tribe: Party,
    players: List<Player>,
    props: PairAssignmentsAnimator
) {
    child(FrameRunner(SpinAnimationState.sequence(pairAssignments), speed = tribe.animationSpeed) { state ->
        val rosteredPairAssignments = rosteredPairAssignments(pairAssignments, players)
        flipperSpinAnimation(state, props, tribe, rosteredPairAssignments)
    })
}

private fun ChildrenBuilder.flipperSpinAnimation(
    state: SpinAnimationState,
    props: PairAssignmentsAnimator,
    tribe: Party,
    rosteredPairAssignments: RosteredPairAssignments
) = Flipper {
    flipKey = state.toString()
    if (state == End)
        props.children(this)
    else
        child(SpinAnimationPanel(tribe, rosteredPairAssignments, state))
}
