package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.graphing.ContributionWindow
import js.objects.Record

fun setWindowSearchParamHandler(setSearchParams: (callback: (Record<String, String?>) -> Record<String, String?>) -> Unit) = { updatedWindow: ContributionWindow? ->
    setSearchParams { previous ->
        previous["window"] = updatedWindow?.name
        previous
    }
}
