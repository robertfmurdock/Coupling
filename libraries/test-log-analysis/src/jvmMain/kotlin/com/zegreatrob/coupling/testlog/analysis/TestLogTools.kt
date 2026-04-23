package com.zegreatrob.coupling.testlog.analysis

enum class TestLogCommand {
    VALIDATE,
    ANALYZE,
}

data class TestLogRequest(
    val command: TestLogCommand,
    val args: List<String>,
)

data class TestLogRunResult(
    val exitCode: Int,
    val outputJson: String,
)

object TestLogTools {
    fun run(request: TestLogRequest): TestLogRunResult {
        val mode = when (request.command) {
            TestLogCommand.VALIDATE -> "validate"
            TestLogCommand.ANALYZE -> "analyze"
        }
        return TestLogRunResult(
            exitCode = 0,
            outputJson = """{"mode":"stub-$mode","status":"ok"}""",
        )
    }
}
