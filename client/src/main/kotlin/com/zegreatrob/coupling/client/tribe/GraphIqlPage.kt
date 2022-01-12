package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.cssDiv
import com.zegreatrob.coupling.client.external.auth0.react.useAuth0Data
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.minreact.child
import com.zegreatrob.react.dataloader.*
import kotlinx.browser.window
import kotlinx.css.TextAlign
import kotlinx.css.height
import kotlinx.css.textAlign
import kotlinx.css.vh
import org.w3c.dom.get
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json

private val graphQlUrl = "${WindowFunctions.window.location.origin}${window["basename"]?.toString()}/api/graphql"

val GraphIQLPage = FC<PageProps> {

    val auth0Data = useAuth0Data()

    cssDiv(css = {
        textAlign = TextAlign.left
        height = 100.vh
    }) {
        child(DataLoader({ auth0Data.getIdTokenClaims() }, { null }) { state: DataLoadState<String?> ->
            when (state) {
                is EmptyState -> div()
                is PendingState -> div()
                is ResolvedState -> GraphiQL {
                    this.fetcher = createGraphiQLFetcher(
                        json(
                            "url" to graphQlUrl, "headers" to json("Authorization" to "Bearer ${state.result}")
                        )
                    )
                }
            }
        })
    }
}.also {
    kotlinext.js.require("graphiql/graphiql.css").unsafeCast()
}

external interface GraphiQLProps : Props {

    var fetcher: (graphQlParams: Json) -> Promise<String>

}

