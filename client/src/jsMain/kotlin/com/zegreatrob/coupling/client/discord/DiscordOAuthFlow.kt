package com.zegreatrob.coupling.client.discord

import com.zegreatrob.coupling.action.BeginDiscordOAuthCommand
import com.zegreatrob.coupling.action.GrantDiscordAccessCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.fire
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.testmints.action.ActionCannon
import js.buffer.ArrayBuffer
import js.promise.await
import js.typedarrays.Uint8Array
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import web.crypto.crypto
import web.encoding.TextEncoder

internal fun newDiscordVerifier(): String = crypto.getRandomValues(Uint8Array<ArrayBuffer>(32)).hex()

internal suspend fun discordVerifierChallenge(verifier: String): String = Uint8Array(crypto.subtle.digestAsync("SHA-256", TextEncoder().encode(verifier)).await()).hex()

private fun Uint8Array<ArrayBuffer>.hex(): String = (0 until length).joinToString("") { get(it).toString(16).padStart(2, '0') }

internal class DiscordOAuthFlow(
    private val cannon: ActionCannon<CouplingSdkDispatcher>,
    private val store: (String, String) -> Unit = { key, value -> window.sessionStorage.setItem(key, value) },
    private val load: (String) -> String? = { window.sessionStorage.getItem(it) },
    private val remove: (String) -> Unit = { window.sessionStorage.removeItem(it) },
) {
    suspend fun begin(partyId: PartyId): String {
        val verifier = newDiscordVerifier()
        val result = requireNotNull(cannon.fire(BeginDiscordOAuthCommand(partyId, discordVerifierChallenge(verifier))))
        store("discord-oauth:${result.state}", Json.encodeToString(PendingDiscordOAuth(partyId.value.toString(), verifier)))
        return result.state
    }

    suspend fun complete(code: String, guildId: String, state: String): VoidResult {
        val key = "discord-oauth:$state"
        val pending = load(key) ?: return VoidResult.Rejected
        return try {
            val details = try {
                Json.decodeFromString<PendingDiscordOAuth>(pending)
            } catch (_: IllegalArgumentException) {
                return VoidResult.Rejected
            }
            cannon.fire(GrantDiscordAccessCommand(code, guildId, PartyId(details.partyId), state, details.verifier))
        } finally {
            remove(key)
        }
    }

    fun cancel(state: String) {
        remove("discord-oauth:$state")
    }
}

@Serializable
private data class PendingDiscordOAuth(val partyId: String, val verifier: String)
