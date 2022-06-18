package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.FrameRunner
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.pairassignments.NewPairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.party.PartyConfigDispatcher
import com.zegreatrob.coupling.client.pin.PinCommandDispatcher
import com.zegreatrob.coupling.client.player.PlayerConfigDispatcher
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.minreact.add
import react.FC

interface NoOpDispatcher :
    PartyConfigDispatcher,
    PlayerConfigDispatcher,
    PinCommandDispatcher,
    DeletePairAssignmentsCommandDispatcher,
    NewPairAssignmentsCommandDispatcher {
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository
}

val styles = useStyles("DemoPage")

val demoSequence by lazy { DemoAnimationState.generateSequence() }

val DemoPage = FC<PageProps> { props ->
    val frameIndex = props.search.get("frame")
    val currentFrame = frameIndex?.toIntOrNull()?.let { demoSequence.toList()[it] }
    if (currentFrame != null) {
        add(DemoPageFrame(currentFrame.data))
    } else {
        add(
            FrameRunner(demoSequence, 1.0) { state: DemoAnimationState ->
                add(DemoPageFrame(state))
            }
        )
    }
}
