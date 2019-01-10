import kotlin.js.JsName

@JsName("spinContext")
fun spinContext(couplingComparisionSyntax: CouplingComparisionSyntax) = object : CreatePairCandidateReportCommandDispatcher {
    override val couplingComparisionSyntax = couplingComparisionSyntax
}