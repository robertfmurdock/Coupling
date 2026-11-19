package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.routing.CouplingRoute
import com.zegreatrob.coupling.client.routing.CouplingRouteProps
import com.zegreatrob.coupling.client.routing.authenticatedRoutes
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import js.objects.unsafeJso
import kotlinx.browser.document
import kotlinx.coroutines.await
import react.FC
import react.ReactElement
import react.create
import react.router.RouteObject
import react.router.RouterProvider
import react.router.createMemoryRouter
import kotlin.test.AfterTest
import kotlin.test.Test

class RouteTitleTest {
    private val originalTitle = document.title

    @AfterTest
    fun cleanUp() {
        cleanup()
        document.title = originalTitle
    }

    @Test
    fun navigationIdentifiesIntegrationAndContributionViews() = asyncSetup(object {
        val config = ClientConfig(false, "test", "example.invalid", "", "test", "", "")
        val expectedRouter = createMemoryRouter(
            config.authenticatedRoutes().filter {
                it.path in setOf(
                    "/:partyId/integrations",
                    "/:partyId/contributions",
                    "/:partyId/contributions/list",
                    "/:partyId/contributions/visualization",
                )
            }.map { route ->
                unsafeJso<RouteObject> {
                    path = route.path
                    val original = route.element.unsafeCast<ReactElement<CouplingRouteProps>>().props
                    element = CouplingRoute.create {
                        title = original.title
                        this.config = original.config
                        rComponent = FC {}
                    }
                }
            }.toTypedArray(),
            unsafeJso { initialEntries = arrayOf("/review-party/contributions") },
        )
    }) {
        document.title = "Coupling Review"
        render(RouterProvider.create { router = expectedRouter })
    } exercise {
        listOf(
            "/review-party/integrations",
            "/review-party/contributions/list",
            "/review-party/contributions/visualization",
            "/review-party/contributions",
        ).map { path ->
            act { expectedRouter.navigate(path) }.await()
            document.title
        }
    } verify { titles ->
        titles.assertIsEqualTo(
            listOf(
                "Coupling Review - Integrations",
                "Coupling Review - Contributions List",
                "Coupling Review - Contributions Visualization",
                "Coupling Review - Contributions Overview",
            ),
        )
    }
}
