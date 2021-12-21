package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.animationsDisabledContext
import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.external.reactfliptoolkit.flipper
import com.zegreatrob.coupling.client.frameRunner
import com.zegreatrob.coupling.client.pairassignments.spin.RosteredPairAssignments.Companion.rosteredPairAssignments
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import react.RBuilder

data class PairAssignmentsAnimatorProps(
    val tribe: Tribe,
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument,
    val enabled: Boolean,
    val children: RBuilder.() -> Unit
) : DataProps

private val animationContextConsumer = animationsDisabledContext.Consumer

val PairAssignmentsAnimator = reactFunction<PairAssignmentsAnimatorProps> { props ->
    val (tribe, players, pairAssignments, enabled) = props
    animationContextConsumer { animationsDisabled: Boolean ->
        if (!animationsDisabled && enabled) {
            frameRunner(SpinAnimationState.sequence(pairAssignments), speed = tribe.animationSpeed) { state ->
                val rosteredPairAssignments = rosteredPairAssignments(pairAssignments, players)
                flipperSpinAnimation(state, props, tribe, rosteredPairAssignments)
            }
        } else {
            props.children(this)
        }
    }
}

fun RBuilder.animator(
    tribe: Tribe,
    players: List<Player>,
    pairAssignments: PairAssignmentDocument,
    enabled: Boolean,
    handler: RBuilder.() -> Unit
) = child(
    PairAssignmentsAnimator,
    PairAssignmentsAnimatorProps(tribe, players, pairAssignments, enabled, handler)
)

private fun RBuilder.flipperSpinAnimation(
    state: SpinAnimationState,
    props: PairAssignmentsAnimatorProps,
    tribe: Tribe,
    rosteredPairAssignments: RosteredPairAssignments
) = flipper(flipKey = state.toString()) {
    if (state == End)
        props.children(this)
    else
        spinAnimation(tribe, rosteredPairAssignments, state)
}
