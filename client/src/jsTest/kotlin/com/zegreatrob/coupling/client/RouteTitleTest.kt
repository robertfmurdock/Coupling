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
        val appTitle = "Coupling Review"
        val destinations = listOf(
            "/:partyId/integrations" to "Integrations",
            "/:partyId/contributions/list" to "Contributions List",
            "/:partyId/contributions/visualization" to "Contributions Visualization",
            "/:partyId/contributions" to "Contributions Overview",
        )
        val config = ClientConfig(false, "test", "example.invalid", "", "test", "", "", "unknown")
        val routes = config.authenticatedRoutes()
        val expectedRouter = createMemoryRouter(
            destinations.map { (path, _) -> routes.single { it.path == path }.withoutPageQueries() }.toTypedArray(),
            unsafeJso { initialEntries = arrayOf("/review-party/contributions") },
        )
    }) {
        document.title = appTitle
        render(RouterProvider.create { router = expectedRouter })
    } exercise {
        destinations.map { (path, _) ->
            act { expectedRouter.navigate(path.replace(":partyId", "review-party")) }.await()
            document.title
        }
    } verifyAnd { titles ->
        titles.assertIsEqualTo(destinations.map { (_, title) -> "$appTitle - $title" })
    } teardown {
        expectedRouter.dispose()
    }
}

private fun RouteObject.withoutPageQueries(): RouteObject {
    val original = element.unsafeCast<ReactElement<CouplingRouteProps>>().props
    val originalPath = path
    return unsafeJso {
        path = originalPath
        element = CouplingRoute.create {
            title = original.title
            config = original.config
            rComponent = FC {}
        }
    }
}
