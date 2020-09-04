package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.animationsDisabledContext
import com.zegreatrob.coupling.client.external.reactfliptoolkit.flipper
import com.zegreatrob.coupling.client.frameRunner
import com.zegreatrob.coupling.client.pairassignments.spin.RosteredPairAssignments.Companion.rosteredPairAssignments
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.reactFunction
import react.RBuilder
import react.RHandler
import react.RProps

data class PairAssignmentsAnimatorProps(
    val tribe: Tribe,
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument?,
    val enabled: Boolean
) : RProps

private val animationContextConsumer = animationsDisabledContext.Consumer

val PairAssignmentsAnimator = reactFunction<PairAssignmentsAnimatorProps> { props ->
    val (tribe, players, pairAssignments, enabled) = props
    animationContextConsumer { animationsDisabled: Boolean ->
        if (!animationsDisabled && enabled && pairAssignments != null && pairAssignments.id == null) {
            frameRunner(SpinAnimationState.sequence(pairAssignments), speed = tribe.animationSpeed) { state ->
                val rosteredPairAssignments = rosteredPairAssignments(pairAssignments, players)
                flipperSpinAnimation(state, props, tribe, rosteredPairAssignments)
            }
        } else {
            props.children()
        }
    }
}

fun RBuilder.animator(
    tribe: Tribe,
    players: List<Player>,
    pairAssignments: PairAssignmentDocument?,
    enabled: Boolean,
    handler: RHandler<PairAssignmentsAnimatorProps>
) = child(
    PairAssignmentsAnimator,
    PairAssignmentsAnimatorProps(tribe, players, pairAssignments, enabled), handler = handler
)

private fun RBuilder.flipperSpinAnimation(
    state: SpinAnimationState,
    props: PairAssignmentsAnimatorProps,
    tribe: Tribe,
    rosteredPairAssignments: RosteredPairAssignments
) = flipper(flipKey = state.toString()) {
    if (state == End)
        props.children()
    else
        spinAnimation(tribe, rosteredPairAssignments, state)
}
