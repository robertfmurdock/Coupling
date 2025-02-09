package com.zegreatrob.coupling.client.graphql

import com.zegreatrob.coupling.client.components.external.auth0.react.useAuth0Data
import com.zegreatrob.coupling.client.components.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.minreact.nfc
import com.zegreatrob.react.dataloader.DataLoadState
import com.zegreatrob.react.dataloader.DataLoader
import com.zegreatrob.react.dataloader.EmptyState
import com.zegreatrob.react.dataloader.PendingState
import com.zegreatrob.react.dataloader.ResolvedState
import emotion.react.css
import kotlinx.browser.window
import org.w3c.dom.get
import react.PropsWithValue
import react.create
import react.dom.html.ReactHTML.div
import web.cssom.TextAlign
import web.cssom.vh

private val graphQlUrl = "${WindowFunctions.window.location.origin}${window["basename"]}/api/graphql"

val GraphIQLPage by nfc<PageProps> {
    val auth0Data = useAuth0Data()
    div {
        css {
            textAlign = TextAlign.left
            height = 100.vh
        }
        DataLoader(
            getDataAsync = { auth0Data.getAccessTokenSilently() },
            errorData = { "" },
            child = { GraphIQLPageLoader.create { value = it } },
        )
    }
}

val GraphIQLPageLoader by nfc<PropsWithValue<DataLoadState<String>>> { props ->
    when (val state = props.value) {
        is EmptyState -> div()
        is PendingState -> +"Loading authorization..."
        is ResolvedState -> GraphiQL {
            this.fetcher = createGraphiQLFetcher(graphQlUrl, state.result)
        }
    }
}
