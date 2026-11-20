package com.zegreatrob.coupling.e2e.visual

import com.zegreatrob.coupling.e2e.diagnostics.nativeError
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.setup
import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.test.Test

class CaptureHooksTest {
    @Test
    fun captureConfigurationPreservesUnownedOptionsAndLeavesOriginalMochaOptionsIntact() = setup(HookWorld()) {
        originalOptions.asDynamic().retries = 3
    } exercise {
        configure()
    } verify { configured ->
        configured.assertIsEqualTo(config)
        (configured.mochaOpts.fgrep == null).assertIsEqualTo(true)
        configured.mochaOpts.timeout.assertIsEqualTo(120000)
        (configured.mochaOpts.asDynamic().retries as Int).assertIsEqualTo(3)
        originalOptions.fgrep.assertIsEqualTo("product test filter")
        originalOptions.timeout.assertIsEqualTo(60000)
    }

    @Test
    fun prepareBeginsEvidenceBeforeCallingExistingHookWithItsArguments() = asyncSetup(HookWorld()) {
        config.onPrepare = { options, capabilities ->
            receivedPrepareArguments = options to capabilities
            calls.add("prepare")
            "synchronous result"
        }
        configure()
    } exercise {
        Promise.resolve(config.onPrepare!!(config, capabilities)).await()
    } verify {
        calls.assertIsEqualTo(listOf("begin", "prepare"))
        receivedPrepareArguments.assertIsEqualTo(config to capabilities)
    }

    @Test
    fun completionCallsExistingHookBeforeFinishingEvidenceWithRunnerExitCode() = asyncSetup(HookWorld()) {
        config.onComplete = { exitCode, options, capabilities, results ->
            receivedCompleteArguments = listOf(exitCode, options, capabilities, results)
            calls.add("complete")
            Promise.resolve(Unit)
        }
        configure()
    } exercise {
        Promise.resolve(config.onComplete!!(7, config, capabilities, results)).await()
    } verify {
        calls.assertIsEqualTo(listOf("complete", "finish:7"))
        receivedCompleteArguments.assertIsEqualTo(listOf(7, config, capabilities, results))
    }

    @Test
    fun rejectedCompletionHookRecordsFailureAndPreservesOriginalError() = asyncSetup(HookWorld()) {
        config.onComplete = { _, _, _, _ -> Promise.reject(hookFailure) }
        configure()
    } exercise {
        runCatching { Promise.resolve(config.onComplete!!(0, config, capabilities, results)).await() }
    } verify { result ->
        result.exceptionOrNull().assertIsEqualTo(hookFailure)
        calls.assertIsEqualTo(listOf("finish:1"))
    }

    @Test
    fun missingOriginalHooksStillBeginAndFinishEvidence() = asyncSetup(HookWorld()) {
        configure()
    } exercise {
        Promise.resolve(config.onPrepare!!(config, capabilities)).await()
        Promise.resolve(config.onComplete!!(0, config, capabilities, results)).await()
    } verify {
        calls.assertIsEqualTo(listOf("begin", "finish:0"))
    }

    @Test
    fun incompleteEvidenceRejectsAnOtherwiseSuccessfulRunner() = asyncSetup(HookWorld()) {
        completeEvidence = false
        configure()
    } exercise {
        runCatching { Promise.resolve(config.onComplete!!(0, config, capabilities, results)).await() }
    } verify { result ->
        result.exceptionOrNull()?.message.assertIsEqualTo("Visual evidence did not complete; inspect manifest")
        calls.assertIsEqualTo(listOf("finish:0"))
    }
}

private class HookWorld {
    val originalOptions = CaptureMochaOptions(fgrep = "product test filter", timeout = 60000)
    val config = CaptureWdioConfig(mochaOpts = originalOptions)
    val capabilities = Any()
    val results = Any()
    val calls = mutableListOf<String>()
    val hookFailure = nativeError("Existing completion hook failed").unsafeCast<Throwable>()
    var completeEvidence = true
    var receivedPrepareArguments: Pair<CaptureWdioConfig, Any?>? = null
    var receivedCompleteArguments: List<Any?>? = null

    fun configure() = configureCaptureHooks(
        config,
        begin = { calls.add("begin") },
        finish = { exitCode ->
            calls.add("finish:$exitCode")
            completeEvidence
        },
    )
}
