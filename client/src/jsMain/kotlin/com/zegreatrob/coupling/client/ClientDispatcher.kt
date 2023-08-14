package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.stats.heatmap.CalculateHeatMapAction
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher

class ClientDispatcher(sdkDispatcher: CouplingSdkDispatcher) :
    CouplingSdkDispatcher by sdkDispatcher,
    CalculateHeatMapAction.Dispatcher
