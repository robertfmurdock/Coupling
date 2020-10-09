package com.zegreatrob.coupling.client.pairassignments

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.StubDispatchFunc
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.findByClass
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.invoke
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class CurrentPairAssignmentsPanelTest {

    private val styles = useStyles("pairassignments/CurrentPairAssignmentsPanel")

    @Test
    fun clickingSaveButtonWillPerformSaveCommandAndRedirectToCurrentPairAssignmentsPage() = setup(object {
        val tribe = stubTribe()
        val pathSetterSpy = SpyData<String, Unit>()
        val pairAssignments = PairAssignmentDocument(
            date = DateTime.now(),
            pairs = emptyList()
        )
        val dispatchFunc = StubDispatchFunc<PairAssignmentsCommandDispatcher>()
        val wrapper = shallow(
            CurrentPairAssignmentsPanel,
            CurrentPairAssignmentsPanelProps(
                tribe,
                pairAssignments,
                { _, _, _ -> },
                { _, _ -> },
                controls = Controls(dispatchFunc, pathSetterSpy::spyFunction) {},
                allowSave = true
            )
        )
    }) exercise {
        wrapper.findByClass(styles["saveButton"]).simulate("click")
        dispatchFunc.simulateSuccess<SavePairAssignmentsCommand>()
    } verify {
        dispatchFunc.commandsDispatched<SavePairAssignmentsCommand>().size
            .assertIsEqualTo(1)
        pathSetterSpy.spyReceivedValues
            .assertContains("/${tribe.id.value}/pairAssignments/current/")
    }

    @Test
    fun clickingDeleteButtonWillPerformDeleteCommandAndReload() = setup(object {
        val tribe = stubTribe()
        val pathSetterSpy = SpyData<String, Unit>()
        val pairAssignments = stubPairAssignmentDoc()
        val dispatchFunc = StubDispatchFunc<PairAssignmentsCommandDispatcher>()
        val wrapper = shallow(
            CurrentPairAssignmentsPanel,
            CurrentPairAssignmentsPanelProps(
                tribe,
                pairAssignments,
                { _, _, _ -> },
                { _, _ -> },
                controls = Controls(dispatchFunc, pathSetterSpy::spyFunction, {}),
                allowSave = true
            )
        )
    }) exercise {
        wrapper.findByClass(styles["deleteButton"]).simulate("click")
        dispatchFunc.simulateSuccess<DeletePairAssignmentsCommand>()
    } verify {
        dispatchFunc.commandsDispatched<DeletePairAssignmentsCommand>()
            .assertIsEqualTo(listOf(DeletePairAssignmentsCommand(tribe.id, pairAssignments.id!!)))
        pathSetterSpy.spyReceivedValues.assertIsEqualTo(
            listOf("/${tribe.id.value}/pairAssignments/current/")
        )
    }

}