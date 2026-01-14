package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.graphing.ContributionWindow
import js.objects.unsafeJso
import tanstack.react.router.useNavigate
import tanstack.react.router.useSearch

fun useWindow(defaultWindow: ContributionWindow): Pair<ContributionWindow, (ContributionWindow?) -> Unit> {
    val search = useSearch()
    val navigate = useNavigate()
    val window: ContributionWindow = search["window"]?.let { window ->
        ContributionWindow.entries.find { it.name == window }
    } ?: defaultWindow
    val setWindow = setWindowSearchParamHandler {
        navigate(unsafeJso {
            this.search = it.asDynamic()
        })
    }
    return Pair(window, setWindow)
}
