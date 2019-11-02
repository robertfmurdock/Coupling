package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.sdk.RepositoryCatalog

interface TestRepositoryProvider : RepositoryCatalog {
    override val tribeRepository get() = throw NotImplementedError("Stubbed for testing.")
    override val playerRepository get() = throw NotImplementedError("Stubbed for testing.")
    override val pairAssignmentDocumentRepository get() = throw NotImplementedError("Stubbed for testing.")
    override val pinRepository get() = throw NotImplementedError("Stubbed for testing.")
}
