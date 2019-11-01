package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.tribe.TribeId

data class User(val email: String, val authorizedTribeIds: Set<TribeId>)