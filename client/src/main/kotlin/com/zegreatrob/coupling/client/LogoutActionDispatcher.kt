package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.axios.axios
import kotlinx.coroutines.await

interface LogoutActionDispatcher {

    suspend fun logout() {
        axios.get("/api/logout").await()
    }

}
