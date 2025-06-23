package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.json.GqlContributionWindow
import js.array.component1
import js.array.component2
import react.router.dom.useSearchParams

fun useWindow(defaultWindow: GqlContributionWindow): Pair<GqlContributionWindow, (GqlContributionWindow?) -> Unit> {
    val (searchParams, setSearchParams) = useSearchParams()
    val window: GqlContributionWindow = searchParams.get("window")?.let { window ->
        GqlContributionWindow.entries.find { it.name == window }
    } ?: defaultWindow
    val setWindow = setWindowSearchParamHandler(setSearchParams)
    return Pair(window, setWindow)
}
