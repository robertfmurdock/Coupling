package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.animationsDisabledContext
import com.zegreatrob.coupling.client.external.react.FRComponent
import com.zegreatrob.coupling.client.external.react.consumer
import com.zegreatrob.coupling.client.external.react.provider
import com.zegreatrob.coupling.client.external.react.reactElement
import com.zegreatrob.coupling.client.external.reactfliptoolkit.flipper
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.pairassignments.spin.FrameRunner.frameRunner
import com.zegreatrob.coupling.client.pairassignments.spin.RosteredPairAssignments.Companion.rosteredPairAssignments
import com.zegreatrob.coupling.client.pairassignments.spin.SpinAnimationPanel.spinAnimation
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import react.RBuilder
import react.RHandler
import react.RProps

data class AnimatorProps(
    val tribe: Tribe,
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument?,
    val enabled: Boolean
) : RProps

object Animator : FRComponent<AnimatorProps>(provider()), WindowFunctions {

    private val animationContextConsumer = animationsDisabledContext.Consumer

    fun RBuilder.animator(
        tribe: Tribe,
        players: List<Player>,
        pairAssignments: PairAssignmentDocument?,
        enabled: Boolean,
        handler: RHandler<AnimatorProps>
    ) = child(
        Animator.component.rFunction,
        AnimatorProps(tribe, players, pairAssignments, enabled), handler = handler
    )

    override fun render(props: AnimatorProps) =
        reactElement {
            val (tribe, players, pairAssignments, enabled) = props

            consumer(animationContextConsumer) { animationsDisabled: Boolean ->
                if (!animationsDisabled && enabled && pairAssignments != null && pairAssignments.id == null) {
                    frameRunner(pairAssignments.stateSequence()) { state ->
                        val rosteredPairAssignments = rosteredPairAssignments(pairAssignments, players)
                        flipperSpinAnimation(state, props, tribe, rosteredPairAssignments)
                    }
                } else {
                    props.children()
                }
            }
        }

    private fun PairAssignmentDocument.stateSequence() =
        generateSequence<Pair<SpinAnimationState, Int>>(Start to 0) { (state, time) ->
            state.next(this)
                ?.let {
                    it to time + state.duration(this)
                }
        }

    private fun RBuilder.flipperSpinAnimation(
        state: SpinAnimationState,
        props: AnimatorProps,
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
