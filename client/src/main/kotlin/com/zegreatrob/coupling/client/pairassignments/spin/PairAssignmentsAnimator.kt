package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.FrameRunner.frameRunner
import com.zegreatrob.coupling.client.animationsDisabledContext
import com.zegreatrob.coupling.client.external.react.FRComponent
import com.zegreatrob.coupling.client.external.react.consumer
import com.zegreatrob.coupling.client.external.react.provider
import com.zegreatrob.coupling.client.external.react.reactElement
import com.zegreatrob.coupling.client.external.reactfliptoolkit.flipper
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.pairassignments.spin.RosteredPairAssignments.Companion.rosteredPairAssignments
import com.zegreatrob.coupling.client.pairassignments.spin.SpinAnimationPanel.spinAnimation
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import react.RBuilder
import react.RHandler
import react.RProps

data class PairAssignmentsAnimatorProps(
    val tribe: Tribe,
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument?,
    val enabled: Boolean
) : RProps

object PairAssignmentsAnimator : FRComponent<PairAssignmentsAnimatorProps>(provider()), WindowFunctions {

    private val animationContextConsumer = animationsDisabledContext.Consumer

    fun RBuilder.animator(
        tribe: Tribe,
        players: List<Player>,
        pairAssignments: PairAssignmentDocument?,
        enabled: Boolean,
        handler: RHandler<PairAssignmentsAnimatorProps>
    ) = child(
        PairAssignmentsAnimator.component.rFunction,
        PairAssignmentsAnimatorProps(tribe, players, pairAssignments, enabled), handler = handler
    )

    override fun render(props: PairAssignmentsAnimatorProps) = reactElement {
        val (tribe, players, pairAssignments, enabled) = props

        consumer(animationContextConsumer) { animationsDisabled: Boolean ->
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

    private fun RBuilder.flipperSpinAnimation(
        state: SpinAnimationState,
        props: PairAssignmentsAnimatorProps,
        tribe: Tribe,
        rosteredPairAssignments: RosteredPairAssignments
    ) = flipper(flipKey = state.toString()) {
        if (state == End)
            props.children()
        else {
            spinAnimation(tribe, rosteredPairAssignments, state)
        }
    }
}
