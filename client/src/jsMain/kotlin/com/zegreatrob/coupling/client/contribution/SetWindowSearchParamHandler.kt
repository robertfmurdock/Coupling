package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.graphing.ContributionWindow
import js.objects.unsafeJso
import react.router.dom.SetURLSearchParams

fun setWindowSearchParamHandler(setSearchParams: SetURLSearchParams) = { updatedWindow: ContributionWindow? ->
    setSearchParams({ previous ->
        previous.also {
            if (updatedWindow != null) {
                previous.set("window", updatedWindow.name)
            } else {
                previous.delete("window")
            }
        }
    }, unsafeJso { })
}
