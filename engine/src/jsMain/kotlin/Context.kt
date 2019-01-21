interface CommandDispatcher : RunGameActionDispatcher

@JsName("spinContext")
fun spinContext(couplingComparisionSyntax: CouplingComparisionSyntax): CommandDispatcher = object : CommandDispatcher,
        FindNewPairsActionDispatcher,
        NextPlayerActionDispatcher,
        CreatePairCandidateReportActionDispatcher,
        CreatePairCandidateReportsActionDispatcher,
        Wheel {
    override val actionDispatcher = this
    override val wheel = this
    override val couplingComparisionSyntax = couplingComparisionSyntax
}

@JsName("spinContext2")
fun spinContext(couplingComparisionSyntax: CouplingComparisionSyntax, jsRepository: dynamic): CommandDispatcher = object : CommandDispatcher,
        ProposeNewPairsCommandDispatcher,
        FindNewPairsActionDispatcher,
        NextPlayerActionDispatcher,
        CreatePairCandidateReportActionDispatcher,
        CreatePairCandidateReportsActionDispatcher,
        Wheel {
    override val repository: CouplingDataRepository = dataRepository(jsRepository)
    override val actionDispatcher = this
    override val wheel = this
    override val couplingComparisionSyntax = couplingComparisionSyntax
}

