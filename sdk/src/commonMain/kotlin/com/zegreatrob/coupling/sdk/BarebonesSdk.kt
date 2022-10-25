package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.RequestSpinActionDispatcher
import com.zegreatrob.coupling.action.user.UserQueryDispatcher

interface BarebonesSdk :
    RepositoryCatalog,
    UserQueryDispatcher,
    RequestSpinActionDispatcher
