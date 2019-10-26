package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.core.entity.tribe.TribeId

data class User(val email: String, val authorizedTribeIds: Set<TribeId>)