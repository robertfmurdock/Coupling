package com.zegreatrob.coupling.client.components.pairassignments

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.minreact.create
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.act
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.waitFor
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import js.core.jso
import korlibs.time.DateTime
import react.ReactNode
import react.create
import react.router.RouterProvider
import react.router.createMemoryRouter
import kotlin.test.Test

class CurrentPairAssignmentsPanelTest {

    @Test
    fun clickingSaveButtonWillNRedirectToCurrentPairAssignmentsPageWithoutSavingBecauseAutosave() = asyncSetup(object {
        val party = stubPartyDetails()
        val pairAssignments = PairAssignmentDocument(
            id = PairAssignmentDocumentId("${uuid4()}"),
            date = DateTime.now(),
            pairs = emptyList(),
        )
        val stubDispatcher = StubDispatcher()
        val actor = UserEvent.setup()
    }) {
        render(
            RouterProvider.create {
                router = createMemoryRouter(
                    arrayOf(
                        jso {
                            path = "/${party.id.value}/pairAssignments/current/"
                            element = ReactNode("current pairs")
                        },
                        jso {
                            path = "*"
                            element = CurrentPairAssignmentsPanel(
                                party,
                                pairAssignments,
                                setPairAssignments = { },
                                allowSave = true,
                                dispatchFunc = stubDispatcher.func(),
                            ).create()
                        },
                    ),
                )
            },
        )
    } exercise {
        actor.click(screen.getByText("Save!"))
        stubDispatcher.sendResult<SavePairAssignmentsCommand, _>(VoidResult.Accepted)
    } verify {
        waitFor {
            stubDispatcher.commandsDispatched<SavePairAssignmentsCommand>().size
                .assertIsEqualTo(0)
            screen.getByText("current pairs")
        }
    }

    @Test
    fun clickingDeleteButtonWillPerformDeleteCommandAndReload() = asyncSetup(object {
        val party = stubPartyDetails()
        val pairAssignments = stubPairAssignmentDoc()
        val stubDispatcher = StubDispatcher()
        val actor = UserEvent.setup()
    }) {
        render(
            Html5DndProvider.create {
                RouterProvider {
                    router = createMemoryRouter(
                        arrayOf(
                            jso {
                                path = "/${party.id.value}/pairAssignments/current/"
                                element = ReactNode("current pairs")
                            },
                            jso {
                                path = "*"
                                element = CurrentPairAssignmentsPanel(
                                    party,
                                    pairAssignments,
                                    setPairAssignments = { },
                                    allowSave = true,
                                    dispatchFunc = stubDispatcher.func(),
                                ).create()
                            },
                        ),
                    )
                }
            },
        )
    } exercise {
        actor.click(screen.findByText("Cancel"))
        act { stubDispatcher.sendResult<DeletePairAssignmentsCommand, _>(VoidResult.Accepted) }
    } verify {
        waitFor {
            stubDispatcher.commandsDispatched<DeletePairAssignmentsCommand>()
                .assertIsEqualTo(listOf(DeletePairAssignmentsCommand(party.id, pairAssignments.id)))
            screen.getByText("current pairs")
        }
    }
}

fun <T> T?.assertNotNull(callback: (T) -> Unit = {}) {
    this.assertIsNotEqualTo(null)
    callback(this!!)
}
