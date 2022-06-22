package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.components.external.auth0.react.useAuth0Data
import com.zegreatrob.minreact.add
import com.zegreatrob.react.dataloader.DataLoadState
import com.zegreatrob.react.dataloader.DataLoader
import com.zegreatrob.react.dataloader.EmptyState
import com.zegreatrob.react.dataloader.PendingState
import com.zegreatrob.react.dataloader.ResolvedState
import csstype.TextAlign
import csstype.vh
import emotion.react.css
import kotlinx.browser.window
import org.w3c.dom.get
import react.FC
import react.dom.html.ReactHTML.div

private val graphQlUrl = "${WindowFunctions.window.location.origin}${window["basename"]}/api/graphql"

val GraphIQLPage = FC<PageProps> {
    val auth0Data = useAuth0Data()
    div {
        css {
            textAlign = TextAlign.left
            height = 100.vh
        }
        add(
            DataLoader({ auth0Data.getAccessTokenSilently() }, { "" }) { state: DataLoadState<String> ->
                when (state) {
                    is EmptyState -> div()
                    is PendingState -> +"Loading authorization..."
                    is ResolvedState -> GraphiQL {
                        this.editorTheme = "dracula"
                        this.fetcher = createGraphiQLFetcher(graphQlUrl, state.result)
                    }
                }
            }
        )
    }
}
