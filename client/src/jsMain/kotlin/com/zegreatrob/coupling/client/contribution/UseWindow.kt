package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.graphing.ContributionWindow
import js.array.component1
import js.array.component2
import react.router.dom.useSearchParams

fun useWindow(defaultWindow: ContributionWindow): Pair<ContributionWindow, (ContributionWindow?) -> Unit> {
    val (searchParams, setSearchParams) = useSearchParams()
    val window: ContributionWindow = searchParams.get("window")?.let { window ->
        ContributionWindow.entries.find { it.name == window }
    } ?: defaultWindow
    val setWindow = setWindowSearchParamHandler(setSearchParams)
    return Pair(window, setWindow)
}
