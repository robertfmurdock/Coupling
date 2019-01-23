interface CommandDispatcher : RunGameActionDispatcher

@JsName("spinContext")
fun spinContext(): CommandDispatcher = object : CommandDispatcher,
        FindNewPairsActionDispatcher,
        NextPlayerActionDispatcher,
        CreatePairCandidateReportActionDispatcher,
        CreatePairCandidateReportsActionDispatcher,
        Wheel {
    override val actionDispatcher = this
    override val wheel = this
}

@JsName("spinContext2")
fun spinContext(jsRepository: dynamic): CommandDispatcher = object : CommandDispatcher,
        ProposeNewPairsCommandDispatcher,
        FindNewPairsActionDispatcher,
        NextPlayerActionDispatcher,
        CreatePairCandidateReportActionDispatcher,
        CreatePairCandidateReportsActionDispatcher,
        Wheel {
    override val repository: CouplingDataRepository = dataRepository(jsRepository)
    override val actionDispatcher = this
    override val wheel = this
}
