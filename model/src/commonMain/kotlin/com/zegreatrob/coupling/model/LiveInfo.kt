package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.user.User

data class LiveInfo(val users: List<User>, val currentPairAssignmentDocument: PairAssignmentDocument?)
