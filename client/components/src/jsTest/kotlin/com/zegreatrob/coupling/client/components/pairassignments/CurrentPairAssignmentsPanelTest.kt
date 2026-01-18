package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.components.external.reactdnd.dndProvider
import com.zegreatrob.coupling.client.components.external.reactdndhtml5backend.html5Backend
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSetId
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPinnedCouplingPair
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.act
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import kotools.types.collection.notEmptyListOf
import react.FC
import tanstack.react.router.RootRouteOptions
import tanstack.react.router.RouteOptions
import tanstack.react.router.RouterOptions
import tanstack.react.router.RouterProvider
import tanstack.react.router.createRootRoute
import tanstack.react.router.createRoute
import tanstack.react.router.createRouter
import tanstack.router.core.RoutePath
import kotlin.test.Test
import kotlin.time.Clock

class CurrentPairAssignmentsPanelTest {

    @Test
    fun clickingSaveButtonWillRedirectToCurrentPairAssignmentsPageWithoutSavingBecauseAutosave() = asyncSetup(object {
        val party = stubPartyDetails()
        val pairAssignments = PairingSet(
            id = PairingSetId.new(),
            date = Clock.System.now(),
            pairs = notEmptyListOf(stubPinnedCouplingPair()),
        )
        val stubDispatcher = StubDispatcher()
        val actor = UserEvent.setup()
        val router = createRouter(
            options = RouterOptions(
                routeTree = createRootRoute(
                    options = RootRouteOptions(notFoundComponent = FC {
                        CurrentPairAssignmentsPanel(
                            party,
                            pairAssignments,
                            setPairAssignments = { },
                            allowSave = true,
                            dispatchFunc = stubDispatcher.func(),
                        )
                    })
                ).also {
                    it.addChildren(
                        arrayOf(
                            createRoute(
                                RouteOptions(
                                    path = RoutePath("/${party.id.value}/pairAssignments/current/"),
                                    getParentRoute = { it },
                                    component = FC { +"current pairs" },
                                ),
                            ),
                        ),
                    )
                },
            ),
        )
    }) {
        render { RouterProvider { router = this@asyncSetup.router } }
    } exercise {
        actor.click(screen.findByText("Save!"))
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
        val router = createRouter(
            options = RouterOptions(
                routeTree = createRootRoute(
                    options = RootRouteOptions(notFoundComponent = FC {
                        CurrentPairAssignmentsPanel(
                            party,
                            pairAssignments,
                            setPairAssignments = { },
                            allowSave = true,
                            dispatchFunc = stubDispatcher.func(),
                        )
                    }),
                ).also {
                    it.addChildren(
                        arrayOf(
                            createRoute(
                                RouteOptions(
                                    path = RoutePath("/${party.id.value}/pairAssignments/current/"),
                                    getParentRoute = { it },
                                    component = FC {
                                        +"current pairs"
                                    },
                                ),
                            ),
                        ),
                    )
                },
            ),
        )
    }) {
        render {
            dndProvider {
                backend = html5Backend
                RouterProvider { router = this@asyncSetup.router }
            }
        }
    } exercise {
        actor.click(screen.findByText("Cancel"))
        act { stubDispatcher.onActionReturn(VoidResult.Accepted) }
    } verify { receivedAction ->
        receivedAction
            .assertIsEqualTo(DeletePairAssignmentsCommand(party.id, pairAssignments.id))
        screen.findByText("current pairs")
    }
}
