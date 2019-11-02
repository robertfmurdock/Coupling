package com.zegreatrob.coupling.client.sdk

interface RepositoryCatalog {
    val tribeRepository: AxiosTribeRepository
    val playerRepository: AxiosPlayerRepository
    val pinRepository: AxiosPinRepository
    val pairAssignmentDocumentRepository: AxiosPairAssignmentsRepository
}

interface AxiosTribeRepository : AxiosGetTribe, AxiosTribeListGet, AxiosTribeSave
interface AxiosPlayerRepository : AxiosPlayerGetter, AxiosPlayerGetDeleted, AxiosPlayerSaver
interface AxiosPinRepository : AxiosPinGetter
interface AxiosPairAssignmentsRepository : AxiosPairAssignmentDocumentGetter, AxiosPairAssignmentDocumentSaver

object AxiosRepositoryCatalog : RepositoryCatalog, AxiosTribeRepository, AxiosPlayerRepository,
    AxiosPairAssignmentsRepository, AxiosPinRepository {
    override val pinRepository get() = this
    override val pairAssignmentDocumentRepository get() = this
    override val playerRepository get() = this
    override val tribeRepository get() = this
}
