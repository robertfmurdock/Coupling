import {mkdir, writeFile, rename} from 'node:fs/promises'
import path from 'node:path'
import {randomUUID} from 'node:crypto'

const EVENTS = ['log.entryAdded', 'network.responseCompleted', 'network.fetchError']
const DEFAULT_LIMITS = {entries: 1000, bytes: 1024 * 1024, entryBytes: 8192}
const LEVELS = {WARNING: 'WARN', WARN: 'WARN', SEVERE: 'ERROR', ERROR: 'ERROR', DEBUG: 'DEBUG', INFO: 'INFO', LOG: 'INFO'}

export function sanitizeUrl(value) {
    try {
        const url = new URL(value)
        if (!['http:', 'https:', 'ws:', 'wss:', 'file:'].includes(url.protocol)) return `[${url.protocol} URL omitted]`
        url.username = ''
        url.password = ''
        url.search = ''
        url.hash = ''
        return url.href
    } catch {
        return '[invalid URL omitted]'
    }
}

function sanitizeText(value) {
    return String(value ?? '')
        .replace(/(?:https?|wss?):\/\/[^\s"<>]+/g, sanitizeUrl)
        .replace(/\bBearer\s+[^\s"',}]+/gi, 'Bearer [redacted]')
        .replace(/("?(?:authorization|cookie|access_token|id_token|refresh_token|password|client_secret)"?\s*[:=]\s*)("[^"]*"|[^\s,;}]+)/gi, '$1"[redacted]"')
}

function identity() {
    const current = globalThis.__couplingCurrentTest
    return current ? {suite: current.suite, test: current.test, testId: current.testId} : undefined
}

function sanitizePayload(value, depth = 0) {
    if (depth > 8) return '[nested data omitted]'
    if (typeof value === 'string') return sanitizeText(value)
    if (Array.isArray(value)) return value.slice(0, 32).map(item => sanitizePayload(item, depth + 1))
    if (value && typeof value === 'object') return Object.fromEntries(Object.entries(value).slice(0, 64).map(([key, item]) => [
        key, /authorization|cookie|token|password|secret|headers|body|storage/i.test(key) ? '[redacted]' : sanitizePayload(item, depth + 1),
    ]))
    return value
}

export function normalizeLog(entry) {
    const message = String(entry.text ?? entry.message ?? '')
    // Kotlin's structured logger can emit JSON through a console method of lower severity.
    let embedded
    const begin = message.indexOf('{')
    if (begin >= 0) {
        try { embedded = JSON.parse(message.slice(begin)) } catch { /* Plain console text. */ }
        if (!embedded) {
            try { embedded = JSON.parse(JSON.parse(message.slice(message.indexOf('"', begin - 1)))) } catch { /* Chrome source prefix. */ }
        }
    }
    const transportLevel = LEVELS[String(entry.level ?? entry.method ?? 'INFO').toUpperCase()] ?? 'INFO'
    const embeddedLevel = LEVELS[String(embedded?.level ?? '').toUpperCase()]
    const level = embeddedLevel === 'ERROR' || transportLevel === 'ERROR' ? 'ERROR'
        : embeddedLevel === 'WARN' || transportLevel === 'WARN' ? 'WARN' : transportLevel
    return {
        source: 'browser', kind: entry.type ?? 'console', level,
        timestamp: entry.timestamp ?? Date.now(), message: embedded ? JSON.stringify(sanitizePayload(embedded)) : sanitizeText(message),
        stack: entry.stackTrace?.callFrames?.slice(0, 12).map(frame => ({
            url: sanitizeUrl(frame.url), functionName: sanitizeText(frame.functionName).slice(0, 200),
            lineNumber: frame.lineNumber, columnNumber: frame.columnNumber,
        })),
    }
}

export function createCollector(browser, options = {}) {
    const limits = {...DEFAULT_LIMITS, ...options.limits}
    const bidi = browser.isBidi
    const directory = options.directory ?? path.resolve('build/reports/browser-diagnostics')
    const getIdentity = options.identity ?? identity
    const deadlineMs = options.deadlineMs ?? 5000
    const storage = {mkdir, writeFile, rename, ...options.storage}
    const severity = {WARN: 0, ERROR: 0}
    let pendingSeverity = {WARN: 0, ERROR: 0}
    let events = [], bytes = 2, sequence = 0, drained = 0, dropped = 0, truncated = 0
    let subscribed = false, stopped = false
    const failures = new WeakMap()
    let bootstrapFailure
    let bootstrapBundle

    function append(record) {
        let entry = {...record, sequence: ++sequence, identity: getIdentity()}
        if (record.source === 'browser' && record.level in severity) {
            severity[record.level]++
            pendingSeverity[record.level]++
        }
        let encoded = JSON.stringify(entry)
        if (Buffer.byteLength(encoded) > limits.entryBytes) {
            truncated++
            entry = {...entry, stack: undefined, identity: undefined, message: String(entry.message ?? '').slice(0, limits.entryBytes / 4), truncated: true}
            encoded = JSON.stringify(entry)
            if (Buffer.byteLength(encoded) > limits.entryBytes) {
                entry = {sequence, source: record.source, level: record.level, message: '[entry truncated]'}
                encoded = JSON.stringify(entry)
            }
        }
        const size = Buffer.byteLength(encoded) + 1
        if (size + 2 > limits.bytes) { dropped++; return }
        while (events.length && (events.length >= limits.entries || bytes + size > limits.bytes)) {
            bytes -= events.shift().size
            dropped++
        }
        events.push({entry, size})
        bytes += size
    }

    const onLog = entry => append(normalizeLog(entry))
    function onNetwork(event, failed) {
        if (!failed && event.response?.status < 400) return
        if (!failed && !event.response?.status) return
        append({source: 'network', timestamp: Date.now(), url: sanitizeUrl(event.request?.url),
            method: String(event.request?.method ?? '').slice(0, 20), status: event.response?.status,
            error: failed ? sanitizeText(event.errorText) : undefined})
    }
    const onResponse = event => onNetwork(event, false)
    const onFetchError = event => onNetwork(event, true)

    async function pollClassic() {
        if (!bidi) for (const entry of await browser.getLogs('browser')) onLog(entry)
    }

    async function start() {
        if (!bidi || subscribed) return
        browser.on('log.entryAdded', onLog)
        browser.on('network.responseCompleted', onResponse)
        browser.on('network.fetchError', onFetchError)
        try {
            await browser.sessionSubscribe({events: EVENTS})
            subscribed = true
        } catch (error) {
            browser.off('log.entryAdded', onLog)
            browser.off('network.responseCompleted', onResponse)
            browser.off('network.fetchError', onFetchError)
            throw error
        }
    }

    async function drainLogs() {
        await pollClassic()
        const logs = events.filter(({entry}) => entry.sequence > drained && entry.source === 'browser').map(({entry}) => entry)
        for (const level of ['WARN', 'ERROR']) {
            const missing = pendingSeverity[level] - logs.filter(entry => entry.level === level).length
            if (missing > 0) logs.push({source: 'browser', level, timestamp: Date.now(), message: `${missing} ${level} entries were evicted; see diagnostic counters`})
        }
        drained = sequence
        pendingSeverity = {WARN: 0, ERROR: 0}
        return logs
    }

    async function capture(error) {
        const filename = path.join(directory, `${Date.now()}-${randomUUID()}.json`)
        const screenshot = filename.replace(/\.json$/, '.png')
        const captureErrors = []
        const controller = new AbortController()
        let snapshot = null, screenshotSaved = false, expired = false, timer, initialWritten = false
        const report = complete => ({sessionId: browser.sessionId, protocol: bidi ? 'bidi' : 'classic', connectionLost: Boolean(bidi && !browser.isBidi), identity: bootstrapFailure?.identity ?? getIdentity(),
            failure: sanitizeText(error?.message ?? error), events: events.map(({entry}) => entry),
            dropped, truncated, severity: {...severity}, snapshot, captureComplete: complete,
            screenshot: screenshotSaved ? path.basename(screenshot) : null,
            captureErrors: complete ? captureErrors : [...captureErrors, 'diagnostic capture incomplete; deadline may have been exceeded']})
        const write = async complete => {
            const temporary = `${filename}.pending`
            await storage.writeFile(temporary, JSON.stringify(report(complete), null, 2), {signal: controller.signal})
            if (!expired) await storage.rename(temporary, filename)
        }
        const commands = async () => {
            await storage.mkdir(directory, {recursive: true})
            if (expired) return
            // Persist evidence before attempting commands on a disconnected browser.
            await write(false)
            initialWritten = true
            if (expired) return
            try { await pollClassic() } catch (failure) { captureErrors.push(`logs: ${sanitizeText(failure.message)}`) }
            if (expired) return
            try {
                const state = await browser.execute(() => ({url: window.location.href,
                    readyState: document.readyState, mountChildren: document.querySelector('.view-container')?.childElementCount ?? null}))
                if (expired) return
                snapshot = {url: sanitizeUrl(state.url), readyState: state.readyState, mountChildren: state.mountChildren}
            } catch (failure) { captureErrors.push(`snapshot: ${sanitizeText(failure.message)}`) }
            if (expired) return
            try {
                await browser.saveScreenshot(screenshot)
                if (expired) return
                screenshotSaved = true
                process.emit('test:screenshot', screenshot)
            } catch (failure) { captureErrors.push(`screenshot: ${sanitizeText(failure.message)}`) }
            if (expired) return
            await write(true)
        }
        const timeout = new Promise((resolve, reject) => {
            timer = setTimeout(() => {
                expired = true
                controller.abort()
                if (initialWritten) resolve()
                else reject(new Error('diagnostic deadline exceeded before evidence could be written'))
            }, deadlineMs)
        })
        try { await Promise.race([commands(), timeout]) }
        finally { clearTimeout(timer) }
        return filename
    }

    async function captureFailure(error) {
        if (bootstrapFailure && bootstrapBundle) return bootstrapBundle
        if (error && typeof error === 'object' && failures.has(error)) return failures.get(error)
        const promise = capture(error)
        if (error && typeof error === 'object') failures.set(error, promise)
        if (bootstrapFailure) bootstrapBundle = promise
        return promise
    }

    async function stop() {
        if (stopped) return
        stopped = true
        browser.off('log.entryAdded', onLog)
        browser.off('network.responseCompleted', onResponse)
        browser.off('network.fetchError', onFetchError)
        if (subscribed && browser.isBidi) await browser.sessionUnsubscribe({events: EVENTS})
    }

    return {start, drainLogs, captureFailure, stop,
        markBootstrapFailure(error) { bootstrapFailure ??= {error, identity: getIdentity()} },
    }
}

async function hooks(previous, context, args) {
    for (const hook of [previous].flat().filter(Boolean)) await hook.apply(context, args)
}

export function configure(config, directories) {
    const previous = {before: config.before, after: config.after, beforeTest: config.beforeTest, afterTest: config.afterTest}
    let collector, currentTest
    config.before = async function (...args) {
        collector = createCollector(globalThis.browser, {directory: path.join(directories.reports, 'browser-diagnostics'), identity: () => identity() ?? currentTest})
        globalThis.browser.couplingDiagnostics = collector
        await collector.start()
        await hooks(previous.before, this, args)
    }
    config.beforeTest = async function (test, context) {
        currentTest = {suite: test.parent, test: test.title, testId: test.uid ?? test.fullTitle ?? test.title}
        await hooks(previous.beforeTest, this, [test, context])
    }
    config.afterTest = async function (test, context, result) {
        try {
            if (!result.passed) await collector.captureFailure(result.error ?? new Error(test.fullTitle ?? test.title))
        } catch (error) {
            // A diagnostic failure must not replace the test's primary exception.
            console.error(`Browser diagnostic collection failed: ${sanitizeText(error.message)}`)
        } finally {
            await hooks(previous.afterTest, this, [test, context, result])
        }
    }
    config.after = async function (...args) {
        try { await hooks(previous.after, this, args) }
        finally { await collector?.stop() }
    }
    return config
}
