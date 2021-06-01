package com.zegreatrob.coupling.repository.validation

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.LiveInfo
import com.zegreatrob.coupling.repository.LiveInfoRepository
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubTribeId
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test

interface LiveInfoRepositoryValidator<R : LiveInfoRepository> : RepositoryValidator<R, SharedContext<R>> {

    @Test
    fun getWillReturnLastSaved() = repositorySetup(object : ContextMint<R>() {
        val tribeId = stubTribeId()
        val liveInfo = LiveInfo(
            listOf(
                CouplingConnection(uuid4().toString(), stubPlayer()),
                CouplingConnection(uuid4().toString(), stubPlayer())
            )
        )
    }.bind()) {
        repository.save(tribeId, liveInfo)
    } exercise {
        repository.get(tribeId)
    } verify { result ->
        result.assertIsEqualTo(liveInfo)
    }

}
