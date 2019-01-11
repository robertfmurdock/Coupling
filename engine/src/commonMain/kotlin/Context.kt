import kotlin.js.JsName

interface CommandDispatcher : GetNextPairActionDispatcher, CreatePairCandidateReportActionDispatcher

@JsName("spinContext")
fun spinContext(couplingComparisionSyntax: CouplingComparisionSyntax): CommandDispatcher = object : CommandDispatcher, CreateAllPairCandidateReportsCommandDispatcher {

    override val actionDispatcher = this
    override val couplingComparisionSyntax = couplingComparisionSyntax
}
