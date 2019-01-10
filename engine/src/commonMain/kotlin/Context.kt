import kotlin.js.JsName

interface CommandDispatcher : CreateAllPairCandidateReportsCommandDispatcher, CreatePairCandidateReportActionDispatcher

@JsName("spinContext")
fun spinContext(couplingComparisionSyntax: CouplingComparisionSyntax) = object : CommandDispatcher {

    override val actionDispatcher = this
    override val couplingComparisionSyntax = couplingComparisionSyntax
}
