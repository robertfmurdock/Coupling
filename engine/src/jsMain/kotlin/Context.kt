interface CommandDispatcher : RunGameActionDispatcher

@JsName("spinContext")
fun spinContext(couplingComparisionSyntax: CouplingComparisionSyntax): CommandDispatcher = object : CommandDispatcher,
        SpinActionDispatcher,
        GetNextPairActionDispatcher,
        CreatePairCandidateReportActionDispatcher,
        CreateAllPairCandidateReportsActionDispatcher,
        Wheel {
    override val actionDispatcher = this
    override val wheel = this
    override val couplingComparisionSyntax = couplingComparisionSyntax
}

