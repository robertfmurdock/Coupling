package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.pairassignmentdocument.SpinDispatcher
import com.zegreatrob.coupling.action.user.UserQueryDispatcher

interface BarebonesSdk : RepositoryCatalog, UserQueryDispatcher, SpinDispatcher
