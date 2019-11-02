package com.zegreatrob.coupling.client.sdk

interface RepositoryCatalog {
    val tribeRepository: AxiosTribeRepository
    val playerRepository: AxiosPlayerRepository
    val pinRepository: AxiosPinRepository
    val pairAssignmentDocumentRepository: AxiosPairAssignmentsRepository
}

interface AxiosTribeRepository : AxiosGetTribe, AxiosTribeListGet
interface AxiosPlayerRepository : AxiosPlayerGetter, AxiosPlayerGetDeleted
interface AxiosPinRepository : AxiosPinGetter
interface AxiosPairAssignmentsRepository : AxiosPairAssignmentDocumentGetter

object AxiosRepositoryCatalog : RepositoryCatalog, AxiosTribeRepository, AxiosPlayerRepository,
    AxiosPairAssignmentsRepository, AxiosPinRepository {
    override val pinRepository get() = this
    override val pairAssignmentDocumentRepository get() = this
    override val playerRepository get() = this
    override val tribeRepository get() = this
}
