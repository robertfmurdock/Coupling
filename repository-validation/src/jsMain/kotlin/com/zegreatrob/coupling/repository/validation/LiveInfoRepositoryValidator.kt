package com.zegreatrob.coupling.repository.validation

import com.zegreatrob.coupling.model.LiveInfo
import com.zegreatrob.coupling.repository.LiveInfoRepository
import com.zegreatrob.coupling.stubmodel.stubTribeId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test

interface LiveInfoRepositoryValidator<R : LiveInfoRepository> : RepositoryValidator<R, SharedContext<R>> {

    @Test
    fun getWillReturnLastSaved() = repositorySetup(object : ContextMint<R>() {
        val tribeId = stubTribeId()
        val liveInfo = LiveInfo(
            listOf(
                stubUser(),
                stubUser()
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
