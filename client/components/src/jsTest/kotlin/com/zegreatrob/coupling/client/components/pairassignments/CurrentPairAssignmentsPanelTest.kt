package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.components.external.reactdnd.dndProvider
import com.zegreatrob.coupling.client.components.external.reactdndhtml5backend.html5Backend
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPinnedCouplingPair
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.act
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import js.objects.unsafeJso
import kotools.types.collection.notEmptyListOf
import react.ReactNode
import react.create
import react.router.RouterProvider
import react.router.createMemoryRouter
import kotlin.test.Test
import kotlin.time.Clock

class CurrentPairAssignmentsPanelTest {

    @Test
    fun clickingSaveButtonWillNRedirectToCurrentPairAssignmentsPageWithoutSavingBecauseAutosave() = asyncSetup(object {
        val party = stubPartyDetails()
        val pairAssignments = PairAssignmentDocument(
            id = PairAssignmentDocumentId.new(),
            date = Clock.System.now(),
            pairs = notEmptyListOf(stubPinnedCouplingPair()),
        )
        val stubDispatcher = StubDispatcher()
        val actor = UserEvent.setup()
    }) {
        render(
            RouterProvider.create {
                router = createMemoryRouter(
                    arrayOf(
                        unsafeJso {
                            path = "/${party.id.value}/pairAssignments/current/"
                            element = ReactNode("current pairs")
                        },
                        unsafeJso {
                            path = "*"
                            element = CurrentPairAssignmentsPanel.create(
                                party,
                                pairAssignments,
                                setPairAssignments = { },
                                allowSave = true,
                                dispatchFunc = stubDispatcher.func(),
                            )
                        },
                    ),
                )
            },
        )
    } exercise {
        actor.click(screen.getByText("Save!"))
    } verify {
        stubDispatcher.receivedActions.size
            .assertIsEqualTo(0)
        screen.getByText("current pairs")
    }

    @Test
    fun clickingDeleteButtonWillPerformDeleteCommandAndReload() = asyncSetup(object {
        val party = stubPartyDetails()
        val pairAssignments = stubPairAssignmentDoc()
        val stubDispatcher = StubDispatcher.Channel()
        val actor = UserEvent.setup()
    }) {
        render {
            dndProvider {
                backend = html5Backend
                RouterProvider {
                    router = createMemoryRouter(
                        arrayOf(
                            unsafeJso {
                                path = "/${party.id.value}/pairAssignments/current/"
                                element = ReactNode("current pairs")
                            },
                            unsafeJso {
                                path = "*"
                                element = CurrentPairAssignmentsPanel.create(
                                    party,
                                    pairAssignments,
                                    setPairAssignments = { },
                                    allowSave = true,
                                    dispatchFunc = stubDispatcher.func(),
                                )
                            },
                        ),
                    )
                }
            }
        }
    } exercise {
        actor.click(screen.findByText("Cancel"))
        act { stubDispatcher.onActionReturn(VoidResult.Accepted) }
    } verify { receivedAction ->
        receivedAction
            .assertIsEqualTo(DeletePairAssignmentsCommand(party.id, pairAssignments.id))
        screen.getByText("current pairs")
    }
}
