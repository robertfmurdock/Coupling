import {test} from 'node:test'
import assert from 'node:assert/strict'
import {EventEmitter} from 'node:events'
import {mkdtemp, readFile, readdir, rm} from 'node:fs/promises'
import {tmpdir} from 'node:os'
import path from 'node:path'
import {createCollector} from '../../../diagnostics/browser-diagnostics.mjs'

class Browser extends EventEmitter {
    isBidi = true
    sessionId = 'fixture-session'
    calls = []
    logs = []
    async sessionSubscribe(options) { this.calls.push(['subscribe', options]) }
    async sessionUnsubscribe(options) { this.calls.push(['unsubscribe', options]) }
    async getLogs() { this.calls.push(['logs']); return this.logs.splice(0) }
    async execute() {
        this.calls.push(['snapshot'])
        return {url: 'https://user:password@example.test/page?token=secret#private', readyState: 'complete', mountChildren: 0}
    }
    async saveScreenshot(filename) { this.calls.push(['screenshot', filename]) }
}

function manualClock(t) {
    t.mock.timers.enable({apis: ['setTimeout']})
    return {expire() { t.mock.timers.tick(5000) }}
}

async function fixture(t, options = {}) {
    const directory = await mkdtemp(path.join(tmpdir(), 'coupling-browser-diagnostics-'))
    t.after(() => rm(directory, {recursive: true, force: true}))
    const browser = new Browser()
    const collector = createCollector(browser, {directory, ...options})
    await collector.start()
    t.after(() => collector.stop())
    return {browser, collector, directory}
}

const log = (text, level = 'error') => ({type: 'console', level, text, timestamp: 1234})

test('captures console and script errors before bootstrap with identity and metadata', async t => {
    const {browser, collector} = await fixture(t, {identity: () => ({suite: 'bootstrap', testId: 'first'})})
    browser.emit('log.entryAdded', {...log('startup failed'), stackTrace: {callFrames: [{url: 'https://example.test/app.js?token=secret', lineNumber: 2, columnNumber: 3, functionName: 'start'}]}})
    const entries = await collector.drainLogs()
    assert.equal(entries.length, 1)
    assert.equal(entries[0].level, 'ERROR')
    assert.equal(entries[0].timestamp, 1234)
    assert.equal(entries[0].identity.testId, 'first')
    assert.equal(entries[0].stack[0].url, 'https://example.test/app.js')
    assert.equal(browser.calls.filter(([call]) => call === 'logs').length, 0)
})

test('classic adapter drains once and preserves timestamp and severity', async t => {
    const browser = new Browser()
    browser.isBidi = false
    browser.logs.push({level: 'SEVERE', timestamp: 42, message: 'page error'})
    const collector = createCollector(browser)
    await collector.start()
    const entries = await collector.drainLogs()
    assert.equal(entries[0].level, 'ERROR')
    assert.equal(entries[0].timestamp, 42)
    assert.equal(browser.calls.length, 1)
    assert.deepEqual(await collector.drainLogs(), [])
    await collector.stop()
})

test('bounded evidence retains severity counts even when entries are evicted', async t => {
    const {browser, collector, directory} = await fixture(t, {limits: {entries: 2, bytes: 800, entryBytes: 250}})
    browser.emit('log.entryAdded', log('first error'))
    for (let i = 0; i < 8; i++) browser.emit('log.entryAdded', log('x'.repeat(1000), 'info'))
    const filename = await collector.captureFailure(new Error('startup'))
    const report = JSON.parse(await readFile(filename, 'utf8'))
    assert.ok(report.events.length <= 2)
    assert.ok(Buffer.byteLength(JSON.stringify(report.events)) <= 802)
    assert.ok(report.dropped > 0)
    assert.ok(report.truncated > 0)
    assert.equal(report.severity.ERROR, 1)
    assert.ok((await collector.drainLogs()).some(entry => entry.level === 'ERROR'))
})

test('network diagnostics exclude successful responses, secrets, and bodies', async t => {
    const {browser, collector} = await fixture(t)
    const request = {url: 'https://user:pass@example.test/script.js?token=secret#hidden', method: 'GET', headers: [{name: 'Authorization', value: 'secret'}], body: 'secret'}
    browser.emit('network.responseCompleted', {request, response: {status: 200, headers: ['secret']}})
    browser.emit('network.responseCompleted', {request, response: {status: 503, headers: ['secret'], body: 'secret'}})
    browser.emit('network.fetchError', {request, errorText: 'connection reset'})
    const filename = await collector.captureFailure(new Error('load failed'))
    const report = JSON.parse(await readFile(filename, 'utf8'))
    assert.equal(report.events.filter(e => e.source === 'network').length, 2)
    assert.ok(report.events.filter(e => e.source === 'network').every(e => e.url === 'https://example.test/script.js'))
    assert.equal(report.snapshot.url, 'https://example.test/page')
    assert.ok(!JSON.stringify(report).includes('secret'))
    assert.deepEqual(report.severity, {WARN: 0, ERROR: 0})
})

test('one failure produces one awaited snapshot and screenshot; success adds no round trips', async t => {
    const {browser, collector, directory} = await fixture(t)
    assert.deepEqual(await collector.drainLogs(), [])
    assert.equal(browser.calls.length, 1)
    const failure = new Error('bootstrap failed')
    const first = await collector.captureFailure(failure)
    assert.equal(await collector.captureFailure(failure), first)
    assert.equal(browser.calls.filter(([name]) => name === 'snapshot').length, 1)
    assert.equal(browser.calls.filter(([name]) => name === 'screenshot').length, 1)
    assert.equal((await readdir(directory)).filter(name => name.endsWith('.json')).length, 1)
})

test('diagnostic deadline retains the initial evidence and skips further browser commands', async t => {
    const clock = manualClock(t)
    const entered = Promise.withResolvers()
    const {browser, collector} = await fixture(t, {})
    browser.emit('log.entryAdded', log('before disconnect'))
    browser.execute = async () => { entered.resolve(); return new Promise(() => {}) }
    const capture = collector.captureFailure(new Error('timeout'))
    await entered.promise
    clock.expire()
    const report = JSON.parse(await readFile(await capture, 'utf8'))
    assert.equal(report.events[0].message, 'before disconnect')
    assert.ok(report.captureErrors.some(e => e.includes('deadline')))
    assert.equal(browser.calls.filter(([name]) => name === 'screenshot').length, 0)
})

test('stopping the collector unsubscribes and removes listeners', async t => {
    const {browser, collector} = await fixture(t)
    await collector.stop()
    assert.equal(browser.listenerCount('log.entryAdded'), 0)
    assert.equal(browser.calls.filter(([name]) => name === 'unsubscribe').length, 1)
})

test('sanitizes decoded Chrome and BiDi structured console payloads', async t => {
    const {browser, collector} = await fixture(t)
    const payload = {level: 'ERROR', access_token: 'token-value', headers: {Authorization: 'credential-value'}, body: 'body-value', message: 'https://user:pass@example.test/x?secret=url-value#private'}
    browser.emit('log.entryAdded', {level: 'INFO', message: `https://example.test/app.js 1:1 ${JSON.stringify(JSON.stringify(payload))}`})
    browser.emit('log.entryAdded', log(JSON.stringify(payload), 'info'))
    const entries = await collector.drainLogs()
    assert.equal(entries.length, 2)
    for (const entry of entries) {
        assert.equal(entry.level, 'ERROR')
        assert.equal(/token-value|credential-value|body-value|url-value/.test(JSON.stringify(entry)), false)
    }
})

test('the total deadline includes stalled artifact storage', async t => {
    const clock = manualClock(t)
    const entered = Promise.withResolvers()
    const {collector} = await fixture(t, {storage: {
        mkdir: async () => { entered.resolve(); return new Promise(() => {}) },
        writeFile: async () => { throw new Error('must not write') },
    }})
    const capture = collector.captureFailure(new Error('failure'))
    await entered.promise
    clock.expire()
    await assert.rejects(capture, /deadline/)
})

test('a screenshot completing after the deadline cannot attach to a subsequent test', async t => {
    const clock = manualClock(t)
    const entered = Promise.withResolvers()
    const finishScreenshot = Promise.withResolvers()
    const {browser, collector} = await fixture(t, {})
    browser.saveScreenshot = async () => { entered.resolve(); return finishScreenshot.promise }
    const attached = []
    const listener = filename => attached.push(filename)
    process.on('test:screenshot', listener)
    t.after(() => process.off('test:screenshot', listener))
    const capture = collector.captureFailure(new Error('failure'))
    await entered.promise
    clock.expire()
    await capture
    finishScreenshot.resolve()
    await new Promise(resolve => setImmediate(resolve))
    assert.deepEqual(attached, [])
})

test('timeout during a final artifact write preserves the valid initial bundle', async t => {
    const clock = manualClock(t)
    const entered = Promise.withResolvers()
    const finishWrite = Promise.withResolvers()
    let writes = 0
    const {collector} = await fixture(t, {storage: {
        writeFile: async (...args) => {
            if (++writes === 2) { entered.resolve(); await finishWrite.promise }
            const {writeFile} = await import('node:fs/promises')
            return writeFile(...args)
        },
    }})
    const capture = collector.captureFailure(new Error('failure'))
    await entered.promise
    clock.expire()
    const filename = await capture
    const report = JSON.parse(await readFile(filename, 'utf8'))
    assert.equal(report.captureComplete, false)
    assert.ok(report.captureErrors.some(error => error.includes('deadline')))
    finishWrite.resolve()
    await new Promise(resolve => setImmediate(resolve))
    assert.deepEqual(JSON.parse(await readFile(filename, 'utf8')), report)
})

test('runner installs capture before bootstrap and captures a shared failure once', async t => {
    const {configure} = await import('../../../diagnostics/browser-diagnostics.mjs')
    const directory = await mkdtemp(path.join(tmpdir(), 'coupling-runner-diagnostics-'))
    t.after(() => rm(directory, {recursive: true, force: true}))
    const browser = new Browser()
    const previousBrowser = globalThis.browser
    globalThis.browser = browser
    t.after(() => { globalThis.browser = previousBrowser })
    let previousAfterCalls = 0
    const config = configure({before() {
        assert.equal(browser.listenerCount('log.entryAdded'), 1)
        browser.emit('log.entryAdded', log('early startup failure'))
    }, afterTest() { previousAfterCalls++ }}, {reports: directory})
    await config.before()
    await config.beforeTest({title: 'first test', parent: 'fixture', uid: 'test-one'})
    browser.couplingDiagnostics.markBootstrapFailure(new Error('bootstrap'))
    await config.afterTest({title: 'first test'}, {}, {passed: false, error: new Error('bootstrap')})
    await config.afterTest({title: 'second test'}, {}, {passed: false, error: new Error('bootstrap')})
    assert.equal(browser.calls.filter(([name]) => name === 'screenshot').length, 1)
    assert.equal(previousAfterCalls, 2)
    await config.after()
    assert.equal(browser.listenerCount('log.entryAdded'), 0)
})

test('a lost BiDi connection does not switch to classic polling or unsubscribe a dead session', async t => {
    const {browser, collector} = await fixture(t)
    browser.emit('log.entryAdded', log('before connection loss'))
    browser.isBidi = false
    browser.getLogs = async () => { throw new Error('classic polling after disconnect') }
    const logs = await collector.drainLogs()
    assert.equal(logs[0].message, 'before connection loss')
    await collector.stop()
    assert.equal(browser.calls.filter(([name]) => name === 'unsubscribe').length, 0)
})
