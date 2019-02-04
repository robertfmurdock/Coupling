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
